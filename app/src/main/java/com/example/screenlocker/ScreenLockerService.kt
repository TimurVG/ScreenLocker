package com.example.screenlocker

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

class ScreenLockerService : Service() {

    private lateinit var overlayView: LockOverlayView

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        overlayView = LockOverlayView(this)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            android.graphics.PixelFormat.TRANSLUCENT
        )

        try {
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            windowManager.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) {
            try {
                (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        fun start(context: android.content.Context) {
            context.startService(Intent(context, ScreenLockerService::class.java))
        }
    }
}