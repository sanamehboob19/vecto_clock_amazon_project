package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.dilaog

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.R
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.DialogSleepWizardBinding
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.SleepSchedule
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

class SleepWizardDialog(private val onComplete: (SleepSchedule) -> Unit) : DialogFragment() {
    private var _binding: DialogSleepWizardBinding? = null
    private val binding get() = _binding!!
    private var isWakeupStep = true
    private val schedule = SleepSchedule()
    private val selectedDays = mutableSetOf(2, 3, 4, 5, 6) // Mon-Fri default

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogSleepWizardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStepUI()
        initDaySelection()

        binding.btnWizSkip.setOnClickListener { dismiss() }
        binding.btnPlus.setOnClickListener { adjustTime(15) }
        binding.btnMinus.setOnClickListener { adjustTime(-15) }
        binding.layoutTimeClick.setOnClickListener { openManualPicker() }

        binding.btnWizNext.setOnClickListener {
            if (isWakeupStep) {
                schedule.isSunriseAlarm = binding.switchWiz.isChecked
                schedule.days = selectedDays.toList()
                isWakeupStep = false
                setupStepUI()
            } else {
                schedule.isBedtimeReminder = binding.switchWiz.isChecked
                onComplete(schedule)
                dismiss()
            }
        }
    }

    private fun setupStepUI() {
        if (isWakeupStep) {
            binding.wizardProgress.progress = 50
            binding.ivWizardIcon.setImageResource(R.drawable.ic_alarm)
            binding.tvWizardTitle.text = "Set a regular wake-up alarm"
            binding.tvWizFeatTitle.text = "Sunrise Alarm"
            binding.tvWizFeatSub.text = "Slowly brighten screen before alarm"
            binding.btnWizNext.text = "Next"
        } else {
            binding.wizardProgress.progress = 100
            binding.ivWizardIcon.setImageResource(R.drawable.ic_bedtime_moon)
            binding.tvWizardTitle.text = "Set Bedtime and silence your device"
            binding.tvWizFeatTitle.text = "Bedtime Mode"
            binding.tvWizFeatSub.text = "Turn screen grayscale"
            binding.btnWizNext.text = "Done"
        }
        updateTimeDisplay()
    }

    private fun openManualPicker() {
        val h = if (isWakeupStep) schedule.wakeUpHour else schedule.bedtimeHour
        val m = if (isWakeupStep) schedule.wakeUpMinute else schedule.bedtimeMinute
        val picker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H).setHour(h).setMinute(m).build()
        picker.addOnPositiveButtonClickListener {
            if (isWakeupStep) { schedule.wakeUpHour = picker.hour; schedule.wakeUpMinute = picker.minute }
            else { schedule.bedtimeHour = picker.hour; schedule.bedtimeMinute = picker.minute }
            updateTimeDisplay()
        }
        picker.show(childFragmentManager, "WIZ_PICKER")
    }

    private fun adjustTime(mins: Int) {
        if (isWakeupStep) {
            val total = (schedule.wakeUpHour * 60 + schedule.wakeUpMinute + mins + 1440) % 1440
            schedule.wakeUpHour = total / 60; schedule.wakeUpMinute = total % 60
        } else {
            val total = (schedule.bedtimeHour * 60 + schedule.bedtimeMinute + mins + 1440) % 1440
            schedule.bedtimeHour = total / 60; schedule.bedtimeMinute = total % 60
        }
        updateTimeDisplay()
    }

    private fun updateTimeDisplay() {
        val h = if (isWakeupStep) schedule.wakeUpHour else schedule.bedtimeHour
        val m = if (isWakeupStep) schedule.wakeUpMinute else schedule.bedtimeMinute
        val displayH = when { h == 0 -> 12; h > 12 -> h - 12; else -> h }
        binding.tvWizTime.text = String.format("%02d:%02d", displayH, m)
        binding.tvWizAmpm.text = if (h >= 12) "pm" else "am"
    }

    private fun initDaySelection() {
        val views = listOf(binding.wizSun, binding.wizMon, binding.wizTue, binding.wizWed, binding.wizThu, binding.wizFri, binding.wizSat)
        views.forEachIndexed { index, tv ->
            val day = index + 1
            updateDayUI(tv, selectedDays.contains(day))
            tv.setOnClickListener {
                if (selectedDays.contains(day)) selectedDays.remove(day) else selectedDays.add(day)
                updateDayUI(tv, selectedDays.contains(day))
            }
        }
    }

    private fun updateDayUI(tv: TextView, sel: Boolean) {
        tv.setBackgroundResource(if (sel) R.drawable.bg_circle_pink else R.drawable.bg_circle_outline)
        tv.setTextColor(if (sel) Color.BLACK else Color.WHITE)
    }
}