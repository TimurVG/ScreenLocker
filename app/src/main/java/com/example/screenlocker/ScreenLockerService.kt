package com.timurg.screenlocker

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.os.Vibrator
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams

class ScreenLockerService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var vibrator: Vibrator

    // Переменные для детекции жестов
    private var circlesCount = 0
    private var lastCircleTime = 0L
    private var startX = 0f
    private var startY = 0f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(50) // Короткая вибрация при включении

        setupOverlay()
    }

    private fun setupOverlay() {
        overlayView = View(this).apply {
            setOnTouchListener { _, event -> handleTouch(event) }
        }

        val params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT,
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                LayoutParams.TYPE_SYSTEM_OVERLAY
            },
            LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        windowManager.addView(overlayView, params)
    }

    private fun handleTouch(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (isCircleGesture(startX, startY, event.x, event.y)) {
                    handleCircleGesture()
                }
            }
        }
        return true // Блокируем все касания
    }

    private fun isCircleGesture(startX: Float, startY: Float, endX: Float, endY: Float): Boolean {
        val dx = endX - startX
        val dy = endY - startY
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
        return distance > 100f // Упрощенная детекция круга
    }

    private fun handleCircleGesture() {
        val now = System.currentTimeMillis()
        if (now - lastCircleTime < 500) {
            if (++circlesCount >= 2) {
                vibrator.vibrate(200) // Длинная вибрация при разблокировке
                stopSelf()
            }
        } else {
            circlesCount = 1
        }
        lastCircleTime = now
    }

    override fun onDestroy() {
        windowManager.removeView(overlayView)
        super.onDestroy()
    }
}