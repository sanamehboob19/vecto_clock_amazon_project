package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.vm

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject



@HiltViewModel
class TimerViewModel @Inject constructor() : ViewModel() {

    private var countDownTimer: CountDownTimer? = null

    private val _timeLeft = MutableStateFlow(0L) // Milliseconds
    val timeLeft = _timeLeft.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    var totalTimeSet = 0L

    fun startTimer(duration: Long) {
        if (duration <= 0) return

        totalTimeSet = duration
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(duration, 10) {
            override fun onTick(millisUntilFinished: Long) {
                _timeLeft.value = millisUntilFinished
            }

            override fun onFinish() {
                _isRunning.value = false
                _timeLeft.value = 0
            }
        }.start()

        _isRunning.value = true
    }

    fun togglePauseResume() {
        if (_isRunning.value) {
            countDownTimer?.cancel()
            _isRunning.value = false
        } else {
            if (_timeLeft.value > 0) {
                startTimer(_timeLeft.value)
            }
        }
    }

    fun resetTimer() {
        countDownTimer?.cancel()
        _isRunning.value = false
        _timeLeft.value = 0
        totalTimeSet = 0
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }
}