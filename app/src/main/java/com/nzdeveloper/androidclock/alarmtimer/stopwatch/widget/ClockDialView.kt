package com.nzdeveloper.androidclock.alarmtimer.stopwatch.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.graphics.*

class ClockDialView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr)
{

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pinkColor = Color.parseColor("#FFB6C1")

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (width.coerceAtMost(height) / 2f) * 0.85f
        val rectF = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        // 1. THE BLEND GLOW (Radial Gradient)
        // This creates the soft pink "aura" behind the numbers
        val gradient = RadialGradient(
            centerX, centerY, radius * 1.2f,
            intArrayOf(Color.parseColor("#26FFB6C1"), Color.TRANSPARENT), // 15% opacity to transparent
            null, Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        paint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, radius * 1.2f, paint)
        paint.shader = null // Reset shader for other drawings

        // 2. THE SEGMENTED DIAL (Subtle Stopwatch Look)
        val circumference = (2 * Math.PI * radius).toFloat()
        val segmentLength = (circumference / 60) - 12f
        val gapLength = 12f

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 15f
        paint.color = pinkColor
        paint.alpha = 60 // Very subtle transparency
        paint.pathEffect = DashPathEffect(floatArrayOf(segmentLength, gapLength), 0f)
        canvas.drawArc(rectF, -90f, 360f, false, paint)

        // 3. THE TOP MARKER (The "Start" tick)
        // A solid, bright vertical line at the very top
        paint.pathEffect = null
        paint.alpha = 255
        paint.strokeWidth = 8f
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = pinkColor

        // Draw the top indicator line
        canvas.drawLine(centerX, centerY - radius - 20f, centerX, centerY - radius + 20f, paint)

        // Add a small glowing dot at the top for extra detail
        paint.setShadowLayer(15f, 0f, 0f, pinkColor)
        canvas.drawCircle(centerX, centerY - radius - 30f, 5f, paint)

        // 4. THIN OUTER BOUNDARY
        paint.clearShadowLayer()
        paint.strokeWidth = 2f
        paint.alpha = 40
        canvas.drawCircle(centerX, centerY, radius + 10f, paint)
    }
}