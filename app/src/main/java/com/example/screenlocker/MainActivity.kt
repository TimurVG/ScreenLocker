package com.example.screenlocker

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import com.example.screenlocker.gesture.LockGestureDetector

class MainActivity : AppCompatActivity() {
    private lateinit var gestureDetector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Проверка разрешений перед запуском
        if (PermissionHelper.checkOverlayPermission(this)) {
            initGestureDetector()
            startService(Intent(this, LockService::class.java))
        }
    }

    private fun initGestureDetector() {
        gestureDetector = GestureDetectorCompat(this,
            LockGestureDetector(this).apply {
                setOnLockListener {
                    startService(Intent(this@MainActivity, LockService::class.java).apply {
                        action = LockService.ACTION_LOCK
                    })
                }
            })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PermissionHelper.OVERLAY_PERMISSION_CODE) {
            if (PermissionHelper.checkOverlayPermission(this)) {
                initGestureDetector()
            }
        }
    }
}