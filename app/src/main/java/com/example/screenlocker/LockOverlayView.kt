package com.example.screenlocker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import android.os.Vibrator

class LockOverlayView(context: Context) : View(context) {
    private var unlockListener: (() -> Unit)? = null
    private val paint = Paint().apply {
        color = Color.TRANSPARENT
        style = Paint.Style.FILL
    }
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private var circleCount = 0

    fun setOnUnlockListener(listener: () -> Unit) {
        unlockListener = listener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.TRANSPARENT)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                circleCount = 0
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                // Детектор кругового жеста (упрощённая версия)
                if (event.historySize > 2) {
                    val x1 = event.getHistoricalX(0)
                    val y1 = event.getHistoricalY(0)
                    val x2 = event.getX()
                    val y2 = event.getY()

                    if (isCircularMotion(x1, y1, x2, y2)) {
                        circleCount++
                        if (circleCount == 2) {
                            vibrator.vibrate(200) // Длинная вибрация
                            unlockListener?.invoke()
                        }
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun isCircularMotion(x1: Float, y1: Float, x2: Float, y2: Float): Boolean {
        // Простая проверка на круговое движение
        val dx = x2 - x1
        val dy = y2 - y1
        return dx * dx + dy * dy > 100 // Пороговое значение
    }
}