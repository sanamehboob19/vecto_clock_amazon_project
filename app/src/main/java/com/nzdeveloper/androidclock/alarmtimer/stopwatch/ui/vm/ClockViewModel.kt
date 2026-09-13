package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.vm

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ClockViewModel @Inject constructor() : ViewModel() {

    // true = Digital, false = Classic
    private val _isDigitalMode = MutableStateFlow(true)
    val isDigitalMode = _isDigitalMode.asStateFlow()

    fun setClockMode(isDigital: Boolean) {
        _isDigitalMode.value = isDigital
    }
}