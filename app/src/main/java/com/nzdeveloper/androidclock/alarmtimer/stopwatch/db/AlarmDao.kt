package com.nzdeveloper.androidclock.alarmtimer.stopwatch.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.Alarm
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: Alarm): Long // This must return Long

    @Query("SELECT * FROM alarms ORDER BY id DESC")
    fun getAllAlarms(): Flow<List<Alarm>>

    @Delete
    suspend fun deleteAlarm(alarm: Alarm)

    @Update
    suspend fun updateAlarm(alarm: Alarm)


    @Query("SELECT * FROM alarms WHERE id = :id")
    fun getAlarmByIdSync(id: Int): Alarm?


}