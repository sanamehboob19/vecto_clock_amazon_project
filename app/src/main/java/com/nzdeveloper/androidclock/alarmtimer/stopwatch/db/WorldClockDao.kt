package com.nzdeveloper.androidclock.alarmtimer.stopwatch.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.WorldClock
import kotlinx.coroutines.flow.Flow

@Dao
interface WorldClockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addCity(city: WorldClock)

    @Query("SELECT * FROM world_clocks")
    fun getAllCities(): Flow<List<WorldClock>>

    @Delete
    suspend fun deleteCity(city: WorldClock)

    @Query("SELECT * FROM world_clocks LIMIT 4")
    fun getAllCitiesSync(): List<WorldClock>

}