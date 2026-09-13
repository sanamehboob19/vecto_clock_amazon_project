package com.nzdeveloper.androidclock.alarmtimer.stopwatch.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.*
import kotlin.math.cos
import kotlin.math.sin


class AnalogClockView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr)
{

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pinkColor = Color.parseColor("#FFB6C1")
    private val secondaryGray = Color.parseColor("#444444")

    init {
        // Essential: Disable hardware acceleration for this view to allow setShadowLayer (glow effects) to render
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (width.coerceAtMost(height) / 2f) * 0.80f

        // 1. Draw the Dial Ticks (The small lines around the clock)
        drawTicks(canvas, centerX, centerY, radius)

        // 2. Get current time with millisecond precision for fluid sweeping motion
        val calendar = Calendar.getInstance()
        val hours = calendar.get(Calendar.HOUR)
        val minutes = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)
        val milliseconds = calendar.get(Calendar.MILLISECOND)

        // Calculate smooth time values
        val smoothSeconds = seconds + (milliseconds / 1000f)
        val smoothMinutes = minutes + (smoothSeconds / 60f)
        val smoothHours = hours + (smoothMinutes / 60f)

        // 3. Draw Hands (Order: Hour -> Minute -> Second)

        // Hour Hand (Short & White, moving continuously)
        drawHand(canvas, centerX, centerY, (smoothHours * 30f), radius * 0.5f, 6f, Color.WHITE)

        // Minute Hand (Long & White, moving continuously)
        drawHand(canvas, centerX, centerY, (smoothMinutes * 6f), radius * 0.78f, 4f, Color.WHITE)

        // Second Hand (Thin & Pink, sweeping extremely smoothly)
        drawHand(canvas, centerX, centerY, (smoothSeconds * 6f), radius * 0.9f, 2.5f, pinkColor, true)

        // 4. Draw Center Dot (Pink with premium glow)
        paint.color = pinkColor
        paint.style = Paint.Style.FILL
        paint.setShadowLayer(15f, 0f, 0f, pinkColor) // Glow for the center dot
        canvas.drawCircle(centerX, centerY, 8f, paint)
        paint.clearShadowLayer()

        // Refresh at ~30 FPS for fluid sweeping hand motion
        postInvalidateDelayed(33)
    }

    private fun drawTicks(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        paint.style = Paint.Style.STROKE
        for (i in 0 until 60) {
            val angle = Math.toRadians((i * 6).toDouble())
            val isHour = i % 5 == 0 // Every 5th tick is an hour mark

            val lineLength = if (isHour) 30f else 12f
            paint.color = if (isHour) Color.WHITE else secondaryGray
            paint.strokeWidth = if (isHour) 3f else 1.5f

            val startX = (cx + (radius - lineLength) * sin(angle)).toFloat()
            val startY = (cy - (radius - lineLength) * cos(angle)).toFloat()
            val stopX = (cx + radius * sin(angle)).toFloat()
            val stopY = (cy - radius * cos(angle)).toFloat()

            canvas.drawLine(startX, startY, stopX, stopY, paint)
        }
    }

    private fun drawHand(canvas: Canvas, cx: Float, cy: Float, angle: Float, length: Float, weight: Float, color: Int, hasGlow: Boolean = false) {
        paint.color = color
        paint.strokeWidth = weight
        paint.strokeCap = Paint.Cap.ROUND

        if (hasGlow) {
            paint.setShadowLayer(18f, 0f, 0f, color)
        } else {
            paint.clearShadowLayer()
        }

        val angleRad = Math.toRadians((angle - 90).toDouble())
        val stopX = (cx + cos(angleRad) * length).toFloat()
        val stopY = (cy + sin(angleRad) * length).toFloat()

        canvas.drawLine(cx, cy, stopX, stopY, paint)
        paint.clearShadowLayer()
    }
}