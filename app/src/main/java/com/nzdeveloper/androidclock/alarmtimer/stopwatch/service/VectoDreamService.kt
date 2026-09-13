package com.nzdeveloper.androidclock.alarmtimer.stopwatch.service

import android.os.Handler
import android.os.Looper
import android.service.dreams.DreamService
import android.view.View
import android.view.LayoutInflater
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.ActivityScreenSaverBinding
import kotlin.random.Random

/**
 * Native Daydream Service for Android / Amazon Fire TV & Tablets.
 * Allows VectoClock to be selected as the system screensaver under Display Settings,
 * activating automatically when the device is charging or idle.
 */
class VectoDreamService : DreamService() {

    private var binding: ActivityScreenSaverBinding? = null
    private val handler = Handler(Looper.getMainLooper())
    private var moveRunnable: Runnable? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // Exit on user interaction/tap
        isInteractive = true
        isFullscreen = true

        binding = ActivityScreenSaverBinding.inflate(LayoutInflater.from(this))
        setContentView(binding!!.root)

        startDriftingAnimation()
    }

    private fun startDriftingAnimation() {
        moveRunnable = Runnable {
            val safeBinding = binding ?: return@Runnable
            val rootWidth = safeBinding.rootScreenSaver.width
            val rootHeight = safeBinding.rootScreenSaver.height
            val clockWidth = safeBinding.layoutClockContainer.width
            val clockHeight = safeBinding.layoutClockContainer.height

            if (rootWidth > 0 && rootHeight > 0) {
                val randomX = Random.nextInt(0, (rootWidth - clockWidth).coerceAtLeast(1)).toFloat()
                val randomY = Random.nextInt(0, (rootHeight - clockHeight).coerceAtLeast(1)).toFloat()

                // Smooth drift animation to prevent screen burn-in
                safeBinding.layoutClockContainer.animate()
                    .x(randomX)
                    .y(randomY)
                    .setDuration(5000)
                    .start()
            }

            moveRunnable?.let { handler.postDelayed(it, 15000) }
        }
        moveRunnable?.let { handler.postDelayed(it, 1000) }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        moveRunnable?.let { handler.removeCallbacks(it) }
        moveRunnable = null
        binding = null
    }
}