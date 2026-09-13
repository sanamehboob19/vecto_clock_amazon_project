package com.nzdeveloper.androidclock.alarmtimer.stopwatch.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class TimerCircleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr)
{

    private val pinkColor = Color.parseColor("#FFB6C1")
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var progress: Float = 1f // 1.0 = full circle, 0.0 = empty
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (width.coerceAtMost(height) / 2f) - 40f
        if (radius <= 0f) return

        val rect = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        // 1. Draw Background Dark Circle
        paint.color = Color.parseColor("#221F1F")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 15f
        canvas.drawCircle(centerX, centerY, radius, paint)

        // 2. Draw Progress Pink Arc
        paint.color = pinkColor
        paint.strokeCap = Paint.Cap.ROUND
        paint.setShadowLayer(30f, 0f, 0f, pinkColor) // The Glow Effect

        val sweepAngle = 360f * progress
        canvas.drawArc(rect, -90f, sweepAngle, false, paint)
        paint.clearShadowLayer()
    }
}