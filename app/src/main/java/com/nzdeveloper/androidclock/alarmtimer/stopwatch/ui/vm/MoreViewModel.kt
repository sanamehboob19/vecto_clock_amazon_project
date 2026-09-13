package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MoreViewModel @Inject constructor() : ViewModel() {

    // Default Data: Bedtime 11:00 PM (23:00), Wakeup 07:00 AM
    val bedtimeHour = MutableStateFlow(23)
    val bedtimeMin = MutableStateFlow(0)

    val wakeupHour = MutableStateFlow(7)
    val wakeupMin = MutableStateFlow(0)

    // Flow to provide the "8h 00m" duration string
    val sleepDuration = combine(bedtimeHour, bedtimeMin, wakeupHour, wakeupMin) { bH, bM, wH, wM ->
        calculateDuration(bH, bM, wH, wM)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "8h 00m")

    private fun calculateDuration(bH: Int, bM: Int, wH: Int, wM: Int): String {
        var diffMinutes = (wH * 60 + wM) - (bH * 60 + bM)
        if (diffMinutes < 0) diffMinutes += 1440 // Handle overnight duration

        val hours = diffMinutes / 60
        val mins = diffMinutes % 60
        return "${hours}h ${String.format("%02d", mins)}m"
    }
}