package com.nzdeveloper.androidclock.alarmtimer.stopwatch.model

data class Lap(
    val number: Int,
    val lapTime: String,     // Time for just this lap
    val overallTime: String  // Total time since the start
)