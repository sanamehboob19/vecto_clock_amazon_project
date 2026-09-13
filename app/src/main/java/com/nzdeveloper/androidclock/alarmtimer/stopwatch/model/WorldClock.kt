package com.nzdeveloper.androidclock.alarmtimer.stopwatch.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "world_clocks")
data class WorldClock(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cityName: String,
    val timeZoneId: String // e.g., "America/New_York"
)