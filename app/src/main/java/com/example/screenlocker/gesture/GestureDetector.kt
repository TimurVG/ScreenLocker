package com.timurvg.screenlocker.gesture

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.view.GestureDetector
import android.view.MotionEvent
import com.timurvg.screenlocker.service.LockService

class LockGestureDetector(
    private val context: Context
) : GestureDetector.SimpleOnGestureListener() {

    private val centerRect: Rect by lazy {
        val displayMetrics = context.resources.displayMetrics
        val centerX = displayMetrics.widthPixels / 2
        val centerY = displayMetrics.heightPixels / 2
        Rect(centerX - 150, centerY - 150, centerX + 150, centerY + 150)
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        if (centerRect.contains(e.x.toInt(), e.y.toInt())) {
            context.startService(
                Intent(context, LockService::class.java).apply {
                    action = LockService.ACTION_LOCK
                }
            )
            return true
        }
        return false
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        return true
    }

    companion object {
        fun create(context: Context): GestureDetector {
            return GestureDetector(context, LockGestureDetector(context))
        }
    }
}