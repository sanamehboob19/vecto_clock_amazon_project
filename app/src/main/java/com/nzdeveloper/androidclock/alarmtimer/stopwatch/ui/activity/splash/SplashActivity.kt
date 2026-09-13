package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.activity.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.OvershootInterpolator
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.ActivitySplashBinding
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.activity.main.MainActivity
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {

    // Store handler+runnable as fields so they can be cleared in onDestroy
    private val splashHandler = Handler(Looper.getMainLooper())
    private var splashRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startAnimations()
        startLoading()
    }

    private fun startAnimations() {
        // 1. Logo Animation: Scale Up & Fade In
        binding.ivLogo.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .alpha(1f)
            .setDuration(1000)
            .setInterpolator(OvershootInterpolator())
            .start()

        // 2. App Name: Fade In with a slight delay
        binding.tvAppName.animate()
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(500)
            .start()

        // 3. Tagline: Slide Up & Fade In
        binding.tvTagline.translationY = 50f
        binding.tvTagline.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .setStartDelay(800)
            .start()
    }

    private fun startLoading() {
        val durationInMillis: Long = 2500
        val incrementDelay: Long = durationInMillis / 100
        var progress = 0

        splashRunnable = object : Runnable {
            override fun run() {
                progress++
                binding.linearProgress.progress = progress

                if (progress < 100) {
                    splashHandler.postDelayed(this, incrementDelay)
                } else {
                    splashHandler.postDelayed({ openMainAct() }, 300)
                }
            }
        }
        splashHandler.postDelayed(splashRunnable!!, incrementDelay)
    }

    private fun openMainAct() {
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onDestroy() {
        // Prevent handler from firing on a destroyed activity
        splashRunnable?.let { splashHandler.removeCallbacks(it) }
        super.onDestroy()
    }
}