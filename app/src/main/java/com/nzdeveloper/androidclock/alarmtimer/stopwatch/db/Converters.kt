package com.nzdeveloper.androidclock.alarmtimer.stopwatch.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromList(list: List<Int>): String = list.joinToString(",")

    @TypeConverter
    fun toList(data: String): List<Int> {
        return if (data.isEmpty()) emptyList() else data.split(",").map { it.toInt() }
    }
}