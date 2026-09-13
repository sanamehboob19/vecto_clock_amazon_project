package com.nzdeveloper.androidclock.alarmtimer.stopwatch.widget


import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.R
import kotlin.math.cos
import kotlin.math.sin

class SleepCircleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr)
{

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pinkColor = Color.parseColor("#FFB6C1")
    private val trackColor = Color.parseColor("#221F1F")

    // Icons
    private val moonIcon = ContextCompat.getDrawable(context, R.drawable.ic_bedtime_moon)
    private val sunIcon = ContextCompat.getDrawable(context, R.drawable.ic_wakeup_sun)

    // Data (Default: 11 PM to 7 AM)
    private var bedtimeHour = 23
    private var bedtimeMin = 0
    private var wakeupHour = 7
    private var wakeupMin = 0

    fun setTimes(bHour: Int, bMin: Int, wHour: Int, wMin: Int) {
        this.bedtimeHour = bHour
        this.bedtimeMin = bMin
        this.wakeupHour = wHour
        this.wakeupMin = wMin
        invalidate() // Redraw
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = width.coerceAtMost(height) / 2f - 60f
        val rectF = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        // 1. Draw Background Track (24h circle)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 20f
        paint.color = trackColor
        paint.pathEffect = null
        canvas.drawCircle(centerX, centerY, radius, paint)

        // 2. Calculate Angles
        // 0 degrees is 12:00 PM (Standard). We want 12:00 AM at the top.
        // Formula: (TotalMinutes / 1440 minutes in day) * 360 degrees
        val bedtimeTotalMin = (bedtimeHour * 60 + bedtimeMin)
        val wakeupTotalMin = (wakeupHour * 60 + wakeupMin)

        var startAngle = (bedtimeTotalMin.toFloat() / 1440f * 360f) - 90f
        var endAngle = (wakeupTotalMin.toFloat() / 1440f * 360f) - 90f

        // Calculate sweep (how long is the sleep)
        var sweepAngle = endAngle - startAngle
        if (sweepAngle < 0) sweepAngle += 360f

        // 3. Draw Pink Progress Arc
        paint.color = pinkColor
        paint.strokeCap = Paint.Cap.ROUND
        paint.setShadowLayer(30f, 0f, 0f, pinkColor) // High-end Glow
        canvas.drawArc(rectF, startAngle, sweepAngle, false, paint)
        paint.clearShadowLayer()

        // 4. Draw Icons (Moon and Sun)
        drawIconAtAngle(canvas, moonIcon, startAngle, centerX, centerY, radius)
        drawIconAtAngle(canvas, sunIcon, endAngle, centerX, centerY, radius)
    }

    private fun drawIconAtAngle(canvas: Canvas, icon: android.graphics.drawable.Drawable?, angle: Float, cx: Float, cy: Float, radius: Float) {
        if (icon == null) return

        val angleRad = Math.toRadians(angle.toDouble())
        val x = (cx + radius * cos(angleRad)).toFloat()
        val y = (cy + radius * sin(angleRad)).toFloat()

        val iconSize = 40
        icon.setBounds(
            (x - iconSize).toInt(),
            (y - iconSize).toInt(),
            (x + iconSize).toInt(),
            (y + iconSize).toInt()
        )

        // Draw a small dark circle behind icon to make it pop
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#121212")
        canvas.drawCircle(x, y, iconSize.toFloat() + 5, paint)

        icon.draw(canvas)
    }
}