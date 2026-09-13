package com.nzdeveloper.androidclock.alarmtimer.stopwatch.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.IBinder
import android.view.View
import android.view.WindowManager
import android.graphics.Color
import android.graphics.PixelFormat

class GrayscaleService : Service() {

    private var overlayView: View? = null
    private lateinit var windowManager: WindowManager

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        if (overlayView == null) {
            overlayView = View(this)

            // MAGIC: Use a semi-transparent grey color to mimic grayscale
            // #88666666 (Adjust transparency if needed)
            overlayView?.setBackgroundColor(Color.parseColor("#99333333"))

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )

            try {
                windowManager.addView(overlayView, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) { e.printStackTrace() }
        }
        super.onDestroy()
    }
}