package com.example.screenlocker

import android.view.MotionEvent
import kotlin.math.pow
import kotlin.math.sqrt

enum class GestureType {
    NONE, SINGLE_CIRCLE, DOUBLE_CIRCLE
}

class GestureDetector(private val listener: GestureListener? = null) {
    private var startX = 0f
    private var startY = 0f
    private var circleCount = 0
    private var lastCircleTime = 0L
    private var isDetectingCircle = false
    private var circlePoints = mutableListOf<Pair<Float, Float>>()

    fun detectGesture(event: MotionEvent): GestureType {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                circlePoints.clear()
                circlePoints.add(Pair(event.x, event.y))
            }
            MotionEvent.ACTION_MOVE -> {
                circlePoints.add(Pair(event.x, event.y))
                if (circlePoints.size > 10) {
                    checkForCircle()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (isDetectingCircle && circleCount > 0) {
                    if (System.currentTimeMillis() - lastCircleTime < 500) {
                        circleCount++
                    } else {
                        circleCount = 1
                    }
                    lastCircleTime = System.currentTimeMillis()

                    if (circleCount >= 2) {
                        circleCount = 0
                        listener?.onGestureDetected(GestureType.DOUBLE_CIRCLE)
                        return GestureType.DOUBLE_CIRCLE
                    } else {
                        listener?.onGestureDetected(GestureType.SINGLE_CIRCLE)
                        return GestureType.SINGLE_CIRCLE
                    }
                }
                isDetectingCircle = false
                circlePoints.clear()
            }
        }
        return GestureType.NONE
    }

    private fun checkForCircle(): Boolean {
        if (circlePoints.size < 10) return false

        val firstPoint = circlePoints.first()
        val lastPoint = circlePoints.last()
        val distance = sqrt(
            (lastPoint.first - firstPoint.first).toDouble().pow(2.0) +
                    (lastPoint.second - firstPoint.second).toDouble().pow(2.0)
        )

        // Если начальная и конечная точки близки - это круг
        if (distance < 50) {
            isDetectingCircle = true
            circleCount++
            circlePoints.clear()
            return true
        }
        return false
    }

    interface GestureListener {
        fun onGestureDetected(gestureType: GestureType)
    }
}