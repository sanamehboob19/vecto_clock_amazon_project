package com.nzdeveloper.androidclock.alarmtimer.stopwatch.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.SleepSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSchedule(schedule: SleepSchedule)

    @Query("SELECT * FROM sleep_schedule WHERE id = 1")
    fun getSchedule(): Flow<SleepSchedule?>

    @Query("SELECT * FROM sleep_schedule WHERE id = 1")
    fun getScheduleSync(): SleepSchedule?
}