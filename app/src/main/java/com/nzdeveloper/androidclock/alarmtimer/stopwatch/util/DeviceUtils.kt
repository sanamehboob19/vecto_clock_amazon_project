package com.nzdeveloper.androidclock.alarmtimer.stopwatch.util

import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration

/**
 * Utility object to detect the type of Amazon/Android device at runtime.
 * Used to adjust UI behavior for Fire TV (D-pad remote) vs Tablet (touch) vs Phone.
 */
object DeviceUtils {

    /**
     * Returns true if the app is running on Amazon Fire TV or any Android TV device.
     * Fire TV uses UiMode = TV and has no touchscreen.
     */
    fun isFireTV(context: Context): Boolean {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        return uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
    }

    /**
     * Returns true if the device is a tablet (sw >= 600dp).
     */
    fun isTablet(context: Context): Boolean {
        return context.resources.configuration.smallestScreenWidthDp >= 600
    }

    /**
     * Returns true if the device has a physical touchscreen.
     * Fire TV returns false here.
     */
    fun hasTouchScreen(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
    }

    /**
     * Returns true if D-pad / remote navigation should be used.
     * This is true for Fire TV and also for tablets connected to a keyboard/remote.
     */
    fun needsDpadNavigation(context: Context): Boolean {
        return isFireTV(context) || !hasTouchScreen(context)
    }
}
