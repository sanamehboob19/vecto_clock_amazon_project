package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.db.SleepDao
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.SleepSchedule
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.service.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BedtimeViewModel @Inject constructor(
    private val sleepDao: SleepDao
) : ViewModel() {

    // Observe this in Activity to update the Circle and Cards
    val schedule: StateFlow<SleepSchedule?> = sleepDao.getSchedule()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveSleepSchedule(context: Context, schedule: SleepSchedule) {
        viewModelScope.launch {
            sleepDao.saveSchedule(schedule)

            // Re-schedule everything based on new settings
            AlarmScheduler.scheduleSleepFlow(context, schedule)
        }
    }
}