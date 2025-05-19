package com.example.screenlocker

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt

class LockScreenActivity : Activity() {
    private val touchPointsX = ArrayList<Float>()
    private val touchPointsY = ArrayList<Float>()
    private lateinit var vibrator: Vibrator
    private var circlesDetected = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_screen)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchPointsX.clear()
                touchPointsY.clear()
            }
            MotionEvent.ACTION_MOVE -> {
                touchPointsX.add(x)
                touchPointsY.add(y)
            }
            MotionEvent.ACTION_UP -> {
                if (isCircle(touchPointsX, touchPointsY)) {
                    circlesDetected++
                    when (circlesDetected) {
                        1 -> {
                            safeVibrate(100)
                            // Блокировка экрана
                        }
                        2 -> {
                            safeVibratePattern(longArrayOf(0, 100, 50, 100), -1)
                            finish() // Разблокировка
                        }
                    }
                }
            }
        }
        return true
    }

    private fun isCircle(xPoints: ArrayList<Float>, yPoints: ArrayList<Float>): Boolean {
        if (xPoints.size < 20) return false

        val centerX = (xPoints.maxOrNull()!! + xPoints.minOrNull()!!) / 2
        val centerY = (yPoints.maxOrNull()!! + yPoints.minOrNull()!!) / 2
        var avgRadius = 0f

        for (i in xPoints.indices) {
            val dx = xPoints[i] - centerX
            val dy = yPoints[i] - centerY
            avgRadius += sqrt(dx * dx + dy * dy)
        }
        avgRadius /= xPoints.size

        val tolerance = avgRadius * 0.25f
        var validPoints = 0

        for (i in xPoints.indices) {
            val dx = xPoints[i] - centerX
            val dy = yPoints[i] - centerY
            val radius = sqrt(dx * dx + dy * dy)
            if (abs(radius - avgRadius) <= tolerance) validPoints++
        }

        return validPoints > xPoints.size * 0.7 && avgRadius > 150f
    }

    private fun safeVibrate(milliseconds: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(milliseconds)
        }
    }

    private fun safeVibratePattern(pattern: LongArray, repeat: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeat))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, repeat)
        }
    }
}