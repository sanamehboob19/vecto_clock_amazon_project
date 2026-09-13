package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.vm

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    // This variable stores the currently selected tab index (0, 1, 2, or 3)
    var currentTabIndex: Int = 0
}