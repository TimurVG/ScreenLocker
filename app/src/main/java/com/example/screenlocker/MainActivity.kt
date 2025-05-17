package com.example.screenlocker

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.*
import android.widget.ImageView
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var switchLock: Switch
    private lateinit var imageLock: ImageView
    private lateinit var windowManager: WindowManager
    private lateinit var lockView: View
    private var isLocked = false
    private val gesturePoints = mutableListOf<Pair<Float, Float>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        switchLock = findViewById(R.id.switchLock)
        imageLock = findViewById(R.id.imageLock)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        switchLock.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startLockService()
                vibrate(100)
                imageLock.setImageResource(R.drawable.ic_lock_closed)
            } else {
                stopLockService()
                vibrate(50)
                imageLock.setImageResource(R.drawable.ic_lock_open)
            }
        }
    }

    private fun startLockService() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        lockView = inflater.inflate(R.layout.lock_overlay, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(lockView, params)

        lockView.setOnTouchListener { _, event ->
            handleGesture(event)
            true
        }
    }

    private fun stopLockService() {
        if (::lockView.isInitialized) {
            windowManager.removeView(lockView)
        }
        isLocked = false
    }

    private fun handleGesture(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> gesturePoints.clear()
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 3) {
                    gesturePoints.add(Pair(event.getX(0), event.getY(0)))
                }
            }
            MotionEvent.ACTION_UP -> {
                if (event.pointerCount == 3) {
                    if (!isLocked && isSwipeDown(gesturePoints)) lockScreen()
                    else if (isLocked && isUnlockGesture(gesturePoints)) unlockScreen()
                }
            }
        }
    }

    private fun isSwipeDown(points: List<Pair<Float, Float>>) =
        points.size >= 2 && points.last().second - points.first().second > 100

    private fun isUnlockGesture(points: List<Pair<Float, Float>>): Boolean {
        if (points.size < 3) return false
        val (first, mid, last) = Triple(points[0], points[1], points[2])
        return (mid.first - first.first > 100) && (last.second - mid.second < -100)
    }

    private fun lockScreen() {
        isLocked = true
        vibrate(200)
    }

    private fun unlockScreen() {
        isLocked = false
        vibrate(200)
    }

    private fun vibrate(durationMs: Long) {
        (getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                it.vibrate(durationMs)
            }
        }
    }
}