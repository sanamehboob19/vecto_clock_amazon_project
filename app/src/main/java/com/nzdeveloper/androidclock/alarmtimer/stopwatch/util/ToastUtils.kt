package com.nzdeveloper.androidclock.alarmtimer.stopwatch.util

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast

object ToastUtils {
    private var currentToast: Toast? = null
    private lateinit var appContext: Context

    /**
     * Initialize with Application Context to prevent memory leaks.
     * Call this once in your Application class.
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * Shows a toast using a Raw String.
     */
    fun show(message: String, isLong: Boolean = false) {
        if (!::appContext.isInitialized) return

        // Cancel the previous toast so they don't stack
        currentToast?.cancel()

        val duration = if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        currentToast = Toast.makeText(appContext, message, duration)
        currentToast?.show()
    }



    /**
     * Shows a toast using a String Resource ID (R.string.xyz).
     */
    fun show(resId: Int, isLong: Boolean = false) {
        if (!::appContext.isInitialized) return
        val message = appContext.getString(resId)
        show(message, isLong)
    }





}