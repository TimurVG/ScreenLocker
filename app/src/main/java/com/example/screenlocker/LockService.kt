package com.example.screenlocker

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.view.WindowManager.LayoutParams

class LockService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var lockView: View

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setupLockView()
        return START_STICKY
    }

    private fun setupLockView() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        lockView = LayoutInflater.from(this).inflate(R.layout.lock_screen, null)

        val params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                LayoutParams.TYPE_PHONE,
            LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        lockView.setOnTouchListener { _, _ ->
            stopSelf()
            true
        }

        windowManager.addView(lockView, params)
    }

    override fun onDestroy() {
        if (::windowManager.isInitialized && ::lockView.isInitialized) {
            windowManager.removeView(lockView)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}