package com.nzdeveloper.androidclock.alarmtimer.stopwatch.model

import androidx.room.Entity
import androidx.room.PrimaryKey



@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var hour: Int,
    var minute: Int,
    var amPm: String,
    var days: List<Int> = emptyList(), // 1=Sun, 2=Mon...
    var isEnabled: Boolean = true,
    var label: String = "Alarm",
    var isVibrate: Boolean = true,
    var ringtoneUri: String? = null // null means it will use the system default
)