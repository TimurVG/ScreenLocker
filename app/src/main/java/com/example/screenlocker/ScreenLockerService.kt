package com.example.screenlocker

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.Toast

class ScreenLockerService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private var circleCount = 0
    private var isLocked = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        setupOverlay()
    }

    private fun setupOverlay() {
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )

        overlayView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    circleCount = 0
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isCircleGesture(event)) {
                        circleCount++
                        when (circleCount) {
                            1 -> {
                                isLocked = true
                                Toast.makeText(this, "Экран заблокирован", Toast.LENGTH_SHORT).show()
                            }
                            2 -> {
                                isLocked = false
                                Toast.makeText(this, "Экран разблокирован", Toast.LENGTH_SHORT).show()
                                stopSelf()
                            }
                        }
                    }
                    isLocked
                }
                else -> isLocked
            }
        }

        windowManager.addView(overlayView, params)
    }

    private fun isCircleGesture(event: MotionEvent): Boolean {
        // Упрощенная проверка кругового жеста
        return event.historySize > 10 && event.pointerCount == 1
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
    }
}