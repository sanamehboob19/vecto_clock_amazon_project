package com.nzdeveloper.androidclock.alarmtimer.stopwatch.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.db.AlarmDao
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.db.AppDatabase
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.db.SleepDao
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.db.WorldClockDao
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.Constant
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.TinyDB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(Constant.PREF_NAME, Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideTinyDB(sharedPreferences: SharedPreferences): TinyDB {
        return TinyDB(sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "vecto_clock_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideAlarmDao(database: AppDatabase): AlarmDao {
        return database.alarmDao()
    }


    @Provides
    fun provideSleepDao(database: AppDatabase): SleepDao {
        return database.sleepDao()
    }

    @Provides
    fun provideWorldClockDao(database: AppDatabase): WorldClockDao {
        return database.worldClockDao()
    }


}