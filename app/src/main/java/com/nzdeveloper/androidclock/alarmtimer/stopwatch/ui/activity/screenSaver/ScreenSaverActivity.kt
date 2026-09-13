package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.activity.screenSaver

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.ActivityScreenSaverBinding
import kotlin.random.Random


class ScreenSaverActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScreenSaverBinding
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var moveRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemUI()

        setLowBrightness()

        binding = ActivityScreenSaverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //  Click anywhere to exit (touch)
        binding.rootScreenSaver.setOnClickListener {
            finish()
        }

        // D-pad / remote SELECT key to exit on Fire TV (no touchscreen)
        binding.rootScreenSaver.isFocusable = true
        binding.rootScreenSaver.requestFocus()
        binding.rootScreenSaver.setOnKeyListener { _, keyCode, event ->
            if (event.action == android.view.KeyEvent.ACTION_DOWN &&
                (keyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER ||
                 keyCode == android.view.KeyEvent.KEYCODE_ENTER ||
                 keyCode == android.view.KeyEvent.KEYCODE_BACK)) {
                finish()
                true
            } else false
        }

        //  Start "Burn-in Protection" Animation
        startDriftingAnimation()
    }

    /**
     * Sets the brightness of this activity to the lowest possible value (0.01)
     */
    private fun setLowBrightness() {
        val layoutParams = window.attributes
        // 0.0f is off (not recommended), 0.01f is the dimmest visible setting
        layoutParams.screenBrightness = 0.01f
        window.attributes = layoutParams
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    private fun startDriftingAnimation() {
        moveRunnable = Runnable {
            val rootWidth = binding.rootScreenSaver.width
            val rootHeight = binding.rootScreenSaver.height
            val clockWidth = binding.layoutClockContainer.width
            val clockHeight = binding.layoutClockContainer.height

            if (rootWidth > 0 && rootHeight > 0) {
                // Calculate random position within screen bounds
                val randomX = Random.nextInt(0, (rootWidth - clockWidth).coerceAtLeast(1)).toFloat()
                val randomY = Random.nextInt(0, (rootHeight - clockHeight).coerceAtLeast(1)).toFloat()

                // Smoothly move the clock to prevent burn-in
                binding.layoutClockContainer.animate()
                    .x(randomX)
                    .y(randomY)
                    .setDuration(5000) // 5 seconds move duration
                    .start()
            }

            // Next move after 15 seconds
            handler.postDelayed(moveRunnable, 15000)
        }
        handler.postDelayed(moveRunnable, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(moveRunnable)
    }
}