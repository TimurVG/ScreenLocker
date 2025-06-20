package com.timurvg.screenlocker

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.*
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast

class LockService : Service() {
    private lateinit var overlayView: View
    private lateinit var windowManager: WindowManager
    private val vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    private var tapCountCenter = 0
    private var tapCountCorner = 0
    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createOverlay()
        showToast("Сервис запущен")
    }

    private fun createOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)

        setupTapAreas()

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            },
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        windowManager.addView(overlayView, params)
    }

    private fun setupTapAreas() {
        overlayView.findViewById<View>(R.id.centerTapArea).setOnClickListener {
            tapCountCenter++
            handler.postDelayed({
                if (tapCountCenter == 2) {
                    vibrate(300)
                    showToast("Экран заблокирован")
                }
                tapCountCenter = 0
            }, 300)
        }

        overlayView.findViewById<View>(R.id.cornerTapArea).setOnClickListener {
            tapCountCorner++
            handler.postDelayed({
                if (tapCountCorner == 4) {
                    vibrate(300)
                    stopSelf()
                }
                tapCountCorner = 0
            }, 500)
        }
    }

    private fun vibrate(duration: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        windowManager.removeView(overlayView)
        handler.removeCallbacksAndMessages(null)
        showToast("Сервис остановлен")
        super.onDestroy()
    }
}