package com.example.screenlocker

import android.content.Context
import android.graphics.Path
import android.os.Vibrator
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import kotlin.math.sqrt

class LockOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val path = Path()
    private var startX = 0f
    private var startY = 0f
    private var circleCount = 0
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private var listener: UnlockListener? = null

    fun setOnUnlockListener(listener: UnlockListener) {
        this.listener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.reset()
                startX = event.x
                startY = event.y
                path.moveTo(startX, startY)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(event.x, event.y)
                if (isCompleteCircle(event.x, event.y)) {
                    circleCount++
                    vibrator.vibrate(50)
                    if (circleCount >= 2) {
                        listener?.onUnlock()
                        return false
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                circleCount = 0
                return true
            }
        }
        return true
    }

    private fun isCompleteCircle(currentX: Float, currentY: Float): Boolean {
        val radius = 150f
        val dx = currentX - startX
        val dy = currentY - startY
        val distance = sqrt((dx * dx + dy * dy).toDouble()).toFloat()

        return distance in (radius * 0.8)..(radius * 1.2)
    }

    interface UnlockListener {
        fun onUnlock()
    }
}