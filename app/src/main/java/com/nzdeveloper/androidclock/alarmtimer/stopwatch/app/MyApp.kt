package com.nzdeveloper.androidclock.alarmtimer.stopwatch.app

import android.app.Application
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.ToastUtils
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        ToastUtils.init(this)
    }
}