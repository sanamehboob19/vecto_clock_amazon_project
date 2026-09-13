package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.dilaog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.R
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.DialogSleepSetupBinding
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.SleepSchedule
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

class SleepSetupDialog(private val onComplete: (SleepSchedule) -> Unit) : DialogFragment() {

    private var _binding: DialogSleepSetupBinding? = null
    private val binding get() = _binding!!

    private var isWakeupStep = false // false = Bedtime Step, true = Wakeup Step
    private val schedule = SleepSchedule()

    private val selectedDays = mutableSetOf(1, 2, 3, 4, 5, 6, 7)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogSleepSetupBinding.inflate(inflater, container, false)

        // 1. Professional Window Setup
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupStepUI()
        initDaySelection() // Initialize the days logic

        // 2. Skip Button Logic
        binding.btnSkip.setOnClickListener { dismiss() }

        // 3. Plus/Minus 15m intervals
        binding.btnPlus.setOnClickListener { adjustTime(15) }
        binding.btnMinus.setOnClickListener { adjustTime(-15) }

        // 4. Manual Time Picker on Text Click
        binding.tvSetupTime.setOnClickListener { openManualPicker() }

        // 5. Navigation Logic (Next / Finish)
        binding.btnNextDone.setOnClickListener {
            if (!isWakeupStep) {
                // Save current toggle state for Bedtime Reminder
                schedule.isBedtimeReminder = binding.switchFeature.isChecked

                // Transition to Wakeup Step
                isWakeupStep = true
                setupStepUI()
            } else {
                // Final Step: Save Sunrise setting and Days
                schedule.isSunriseAlarm = binding.switchFeature.isChecked
                schedule.days = selectedDays.toList() // Finalize days selection

                onComplete(schedule)
                dismiss()
            }
        }
    }

    /**
     * Handles the day-to-day selection logic with UI updates
     */
    private fun initDaySelection() {
        val daysMap = mapOf(
            binding.daySun to 1, binding.dayMon to 2, binding.dayTue to 3,
            binding.dayWed to 4, binding.dayThu to 5, binding.dayFri to 6, binding.daySat to 7
        )

        daysMap.forEach { (view, dayIndex) ->
            // Set initial state (All Pink by default)
            updateDayCircleUI(view, selectedDays.contains(dayIndex))

            view.setOnClickListener {
                if (selectedDays.contains(dayIndex)) {
                    selectedDays.remove(dayIndex)
                } else {
                    selectedDays.add(dayIndex)
                }
                updateDayCircleUI(view, selectedDays.contains(dayIndex))
            }
        }
    }

    private fun updateDayCircleUI(tv: TextView, isSelected: Boolean) {
        if (isSelected) {
            tv.setBackgroundResource(R.drawable.bg_circle_pink)
            tv.setTextColor(Color.BLACK)
        } else {
            tv.setBackgroundResource(R.drawable.bg_circle_outline)
            tv.setTextColor(Color.WHITE)
        }
    }

    private fun setupStepUI() {
        if (!isWakeupStep) {
            // BEDTIME STEP
            binding.ivSetupIcon.setImageResource(R.drawable.ic_bedtime_moon)
            binding.tvSetupTitle.text = "When do you want to sleep?"
            binding.tvFeatureTitle.text = "Bedtime Reminder"
            binding.tvFeatureSub.text = "Get notified 15 mins before sleep"
            binding.switchFeature.isChecked = true // Default ON
            binding.btnNextDone.text = "Next"
        } else {
            // WAKEUP STEP
            binding.ivSetupIcon.setImageResource(R.drawable.ic_wakeup_sun)
            binding.tvSetupTitle.text = "When do you want to wake up?"
            binding.tvFeatureTitle.text = "Sunrise Alarm"
            binding.tvFeatureSub.text = "Glow screen 15 mins before ring"
            binding.switchFeature.isChecked = true // Default ON
            binding.btnNextDone.text = "Finish"
        }
        updateTimeDisplay()
    }

    private fun adjustTime(minutes: Int) {
        if (!isWakeupStep) {
            val total = (schedule.bedtimeHour * 60 + schedule.bedtimeMinute + minutes + 1440) % 1440
            schedule.bedtimeHour = total / 60
            schedule.bedtimeMinute = total % 60
        } else {
            val total = (schedule.wakeUpHour * 60 + schedule.wakeUpMinute + minutes + 1440) % 1440
            schedule.wakeUpHour = total / 60
            schedule.wakeUpMinute = total % 60
        }
        updateTimeDisplay()
    }

    private fun openManualPicker() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(if (isWakeupStep) schedule.wakeUpHour else schedule.bedtimeHour)
            .setMinute(if (isWakeupStep) schedule.wakeUpMinute else schedule.bedtimeMinute)
            .setTitleText(if (isWakeupStep) "Wake up time" else "Bedtime")
            .build()

        picker.addOnPositiveButtonClickListener {
            if (isWakeupStep) {
                schedule.wakeUpHour = picker.hour
                schedule.wakeUpMinute = picker.minute
            } else {
                schedule.bedtimeHour = picker.hour
                schedule.bedtimeMinute = picker.minute
            }
            updateTimeDisplay()
        }
        picker.show(childFragmentManager, "MANUAL_PICKER")
    }

    private fun updateTimeDisplay() {
        val h = if (isWakeupStep) schedule.wakeUpHour else schedule.bedtimeHour
        val m = if (isWakeupStep) schedule.wakeUpMinute else schedule.bedtimeMinute

        val displayH = if (h > 12) h - 12 else if (h == 0) 12 else h
        binding.tvSetupTime.text = String.format("%02d:%02d", displayH, m)
        binding.tvSetupAmPm.text = if (h >= 12) "PM" else "AM"
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val width = (resources.displayMetrics.widthPixels * 0.98).toInt()
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.CENTER)
            window.attributes.windowAnimations = android.R.style.Animation_Dialog
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}