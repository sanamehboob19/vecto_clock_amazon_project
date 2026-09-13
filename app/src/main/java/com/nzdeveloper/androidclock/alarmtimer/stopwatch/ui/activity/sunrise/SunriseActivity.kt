package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.activity.sunrise


import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import android.app.KeyguardManager
import android.content.Context
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.ActivitySunriseBinding

@AndroidEntryPoint
class SunriseActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySunriseBinding
    private var sunriseTimer: CountDownTimer? = null
    private var currentBrightness = 0.05f // Start very low

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Setup Flags to wake screen up even if locked
        setupSunriseFlags()

        binding = ActivitySunriseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Start the 15-minute Sunrise Simulation
        startSunriseSimulation()
    }

    private fun startSunriseSimulation() {
        // Total Time: 15 minutes (900,000 ms)
        // Interval: Every 10 seconds (10,000 ms) for smooth transition
        val totalMillis = 15 * 60 * 1000L

        sunriseTimer = object : CountDownTimer(totalMillis, 10000) {
            override fun onTick(millisUntilFinished: Long) {
                val elapsedTime = totalMillis - millisUntilFinished
                val progress = elapsedTime.toFloat() / totalMillis.toFloat()

                // A. Increase Screen Brightness (0.0 to 1.0)
                currentBrightness = progress.coerceAtLeast(0.05f)
                val layoutParams = window.attributes
                layoutParams.screenBrightness = currentBrightness
                window.attributes = layoutParams

                // B. Increase Glow Alpha (0.0 to 1.0)
                binding.viewSunriseGlow.alpha = progress
            }

            override fun onFinish() {
                // Ensure full brightness at the end
                val layoutParams = window.attributes
                layoutParams.screenBrightness = 1.0f
                window.attributes = layoutParams
                binding.viewSunriseGlow.alpha = 1.0f

            }
        }.start()
    }

    private fun setupSunriseFlags() {
        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    override fun onDestroy() {
        sunriseTimer?.cancel()
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish() // Allow user to exit if they wake up early
    }
}