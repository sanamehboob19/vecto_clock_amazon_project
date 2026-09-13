package com.nzdeveloper.androidclock.alarmtimer.stopwatch.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_schedule")
data class SleepSchedule(
    @PrimaryKey val id: Int = 1, // We only ever have one schedule
    var wakeUpHour: Int = 7,
    var wakeUpMinute: Int = 0,
    var bedtimeHour: Int = 23,
    var bedtimeMinute: Int = 0,
    var days: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7), // 1=Sun, 7=Sat
    var isEnabled: Boolean = true,
    var isSunriseAlarm: Boolean = true,
    var isBedtimeReminder: Boolean = true
)