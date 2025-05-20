package com.timurg.screenlocker

import android.content.Context
import android.util.AttributeSet
import android.view.View

class LockOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var unlockListener: (() -> Unit)? = null

    fun setOnUnlockListener(listener: () -> Unit) {
        this.unlockListener = listener
        // Ваша логика обработки жестов
    }

    // Добавьте методы для показа/скрытия
    fun show() {
        // Логика показа
    }

    fun hide() {
        // Логика скрытия
    }
}