package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.db.AlarmDao
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.Alarm
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.service.AlarmScheduler
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.ToastUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val alarmDao: AlarmDao
) : ViewModel()
{

    val allAlarms: StateFlow<List<Alarm>> = alarmDao.getAllAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateAlarm(context: Context, alarm: Alarm) {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Update Database FIRST
            alarmDao.updateAlarm(alarm)

            // 2. Schedule with System AFTER DB update is confirmed
            withContext(Dispatchers.Main) {
                if (alarm.isEnabled) {
                    AlarmScheduler.schedule(context, alarm)
                } else {
                    AlarmScheduler.cancelById(context, alarm.id)
                }
            }
        }
    }

    fun toggleAlarm(context: Context, alarm: Alarm, isEnabled: Boolean) {
        val updatedAlarm = alarm.copy(isEnabled = isEnabled)
        updateAlarm(context, updatedAlarm) // Re-use the update logic for safety
    }

    fun deleteAlarm(context: Context, alarm: Alarm) {
        viewModelScope.launch(Dispatchers.IO) {
            alarmDao.deleteAlarm(alarm)
            withContext(Dispatchers.Main) {
                AlarmScheduler.cancelById(context, alarm.id)
            }
        }
    }

    fun addAlarm(context: Context, alarm: Alarm) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = alarmDao.insertAlarm(alarm).toInt() // Get the real ID from Room
            val savedAlarm = alarm.copy(id = id)
            withContext(Dispatchers.Main) {
                if (savedAlarm.isEnabled) {
                    AlarmScheduler.schedule(context, savedAlarm)
                }
            }
        }
    }
}

