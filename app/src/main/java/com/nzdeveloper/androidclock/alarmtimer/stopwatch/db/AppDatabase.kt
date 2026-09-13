package com.nzdeveloper.androidclock.alarmtimer.stopwatch.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.Alarm
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.SleepSchedule
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.WorldClock



@Database(entities = [Alarm::class, SleepSchedule::class, WorldClock::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao
    abstract fun sleepDao(): SleepDao
    abstract fun worldClockDao(): WorldClockDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vecto_clock_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}