package com.example.screenlocker

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.*
import android.view.*
import android.widget.Toast
import kotlin.math.abs

class ScreenLockerService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private val vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VibratorManager::class.java)
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    // Жесты
    private var isLocked = false
    private var lastTapTime = 0L
    private val tapTimeout = 300L
    private val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean = true

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (!isLocked || e1 == null) return false

            val deltaX = e2.x - e1.x
            val deltaY = e2.y - e1.y

            // Проверка Z-жеста (вправо → вниз → влево)
            if (abs(deltaX) > 100 && abs(deltaY) > 100) {
                if (deltaX > 0 && deltaY > 0) {
                    unlockScreen()
                    return true
                }
            }
            return false
        }
    })

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        setupOverlay()
    }

    private fun setupOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        overlayView = object : View(this) {
            override fun onTouchEvent(event: MotionEvent): Boolean {
                gestureDetector.onTouchEvent(event)

                when (event.actionMasked) {
                    MotionEvent.ACTION_POINTER_DOWN -> {
                        if (event.pointerCount == 3) {
                            checkForTripleTap()
                        }
                    }
                }
                return true
            }
        }.apply {
            setBackgroundColor(0x00000000) // Прозрачный фон
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        try {
            windowManager.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun checkForTripleTap() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTapTime < tapTimeout) {
            toggleLockState()
        }
        lastTapTime = currentTime
    }

    private fun toggleLockState() {
        isLocked = !isLocked
        vibrate(if (isLocked) 150 else 100)
        showToast(if (isLocked) "Экран заблокирован" else "Экран разблокирован")
    }

    private fun unlockScreen() {
        isLocked = false
        vibrate(100)
        showToast("Экран разблокирован жестом")
    }

    private fun vibrate(duration: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (::windowManager.isInitialized && ::overlayView.isInitialized) {
                windowManager.removeView(overlayView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}