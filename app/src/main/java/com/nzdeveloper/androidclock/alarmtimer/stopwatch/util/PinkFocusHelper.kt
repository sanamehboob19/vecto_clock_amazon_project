package com.nzdeveloper.androidclock.alarmtimer.stopwatch.util

import android.graphics.Color
import android.view.View
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

/**
 * Global Pink D-pad Focus Highlight System.
 * Apply to any View/Button/Card to get:
 *  - Pink stroke border on focus
 *  - Subtle scale-up on focus
 *  - Pink text/icon tint on MaterialButton when focused
 */

private const val PINK = "#FFB6C1"
private const val PINK_BG = "#1AFFB6C1"       // 10% alpha pink background fill
private const val DEFAULT_STROKE = "#2C2C2C"
private const val SCALE_UP = 1.08f
private const val SCALE_NORMAL = 1.0f
private const val ANIM_DURATION = 140L

fun View.applyPinkFocus() {
    setOnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            animate().scaleX(SCALE_UP).scaleY(SCALE_UP).setDuration(ANIM_DURATION).start()
            when (this) {
                is MaterialCardView -> {
                    strokeColor = Color.parseColor(PINK)
                    setCardBackgroundColor(Color.parseColor(PINK_BG))
                }
                is MaterialButton -> {
                    // For dark-background buttons: show a pink stroke outline
                    strokeColor = android.content.res.ColorStateList.valueOf(Color.parseColor(PINK))
                    strokeWidth = 3
                }
                else -> {
                    background?.setTint(Color.parseColor(PINK_BG))
                }
            }
        } else {
            animate().scaleX(SCALE_NORMAL).scaleY(SCALE_NORMAL).setDuration(ANIM_DURATION).start()
            when (this) {
                is MaterialCardView -> {
                    strokeColor = Color.parseColor(DEFAULT_STROKE)
                    // Restore original background — caller should set via tag or default
                    setCardBackgroundColor(Color.parseColor("#1B1B1F"))
                }
                is MaterialButton -> {
                    strokeColor = android.content.res.ColorStateList.valueOf(Color.parseColor(DEFAULT_STROKE))
                    strokeWidth = 1
                }
                else -> {
                    background?.setTintList(null)
                }
            }
        }
    }
}

/** Apply pink focus to a list of views at once */
fun applyPinkFocusToAll(vararg views: View) {
    views.forEach { it.applyPinkFocus() }
}
