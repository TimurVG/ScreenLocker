package com.timurg.screenlocker

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.os.Vibrator
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat

class LockService : Service() {
    private lateinit var overlayView: View
    private lateinit var windowManager: WindowManager
    private val vibrator by lazy { getSystemService(Vibrator::class.java) }
    private var tapCount = 0
    private var lastTapTime = 0L
    private val tapDelay = 300L // 300ms между тапами
    private val unlockZone = 100 // 100x100px зона разблокировки

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1, NotificationHelper.createNotification(this))
        setupOverlay()
    }

    private fun setupOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_lock, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            alpha = 0.05f // 5% прозрачности
        }

        overlayView.setOnTouchListener { v, event ->
            handleTouch(event.x, event.y)
            true
        }

        windowManager.addView(overlayView, params)
    }

    private fun handleTouch(x: Float, y: Float) {
        val currentTime = System.currentTimeMillis()

        // Сброс счетчика если тапы слишком медленные
        if (currentTime - lastTapTime > tapDelay) {
            tapCount = 0
        }

        lastTapTime = currentTime
        tapCount++

        // Блокировка: двойной тап в центре (зона 300x300px)
        if (tapCount == 2 && isInCenterZone(x, y)) {
            vibrator.vibrate(300)
            setOverlayTouchable(true)
            tapCount = 0
        }

        // Разблокировка: 4 тапа в левый верхний угол
        if (tapCount >= 4 && isInUnlockZone(x, y)) {
            vibrator.vibrate(300)
            stopSelf() // Останавливаем сервис
        }
    }

    private fun isInCenterZone(x: Float, y: Float): Boolean {
        val centerX = overlayView.width / 2
        val centerY = overlayView.height / 2
        return x in (centerX - 150)..(centerX + 150) &&
                y in (centerY - 150)..(centerY + 150)
    }

    private fun isInUnlockZone(x: Float, y: Float): Boolean {
        return x <= unlockZone && y <= unlockZone
    }

    private fun setOverlayTouchable(touchable: Boolean) {
        val params = overlayView.layoutParams as WindowManager.LayoutParams
        params.flags = if (touchable) {
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        } else {
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        }
        windowManager.updateViewLayout(overlayView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            windowManager.removeView(overlayView)
        } catch (e: Exception) {
            // Игнорируем ошибки если view не прикреплено
        }
    }
}