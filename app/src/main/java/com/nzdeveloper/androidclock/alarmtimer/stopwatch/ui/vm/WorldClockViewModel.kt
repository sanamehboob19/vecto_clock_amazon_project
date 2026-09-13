package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.vm

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.db.WorldClockDao
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.WorldClock
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.service.WidgetDualStacked
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.service.WidgetWorldClock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorldClockViewModel @Inject constructor(
    private val worldClockDao: WorldClockDao,
    private val application: Application // Hilt automatically provides this
) : ViewModel() {

    val allCities = worldClockDao.getAllCities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCity(city: WorldClock) {
        viewModelScope.launch {
            worldClockDao.addCity(city)

            WidgetWorldClock.refreshWidget(application)
            WidgetDualStacked.refreshWidget(application)
        }
    }

    fun deleteCity(city: WorldClock) {
        viewModelScope.launch {
            worldClockDao.deleteCity(city)

            WidgetWorldClock.refreshWidget(application)
            WidgetDualStacked.refreshWidget(application)
        }
    }
}