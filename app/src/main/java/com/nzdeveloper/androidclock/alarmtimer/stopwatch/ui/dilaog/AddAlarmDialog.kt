package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.dilaog

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.R
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.DialogAddAlarmBinding


class AddAlarmDialog(
    private val onSave: (hour: Int, minute: Int, selectedDays: List<Int>) -> Unit
) : DialogFragment()
{

    private var _binding: DialogAddAlarmBinding? = null
    private val binding get() = _binding!!

    // Track selected days (1=Sun, 2=Mon ... 7=Sat)
    private val selectedDays = mutableSetOf<Int>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogAddAlarmBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTimePicker()
        setupDaySelection()

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener {
            onSave(binding.timePicker.hour, binding.timePicker.minute, selectedDays.toList())
            dismiss()
        }
    }

    private fun setupTimePicker() {
        binding.timePicker.setOnTimeChangedListener { _, hour, minute ->
            val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
            val amPm = if (hour >= 12) "PM" else "AM"
            binding.tvSelectedTime.text = String.format("%02d:%02d", displayHour, minute)
            binding.tvAmPm.text = amPm
        }
    }

    private fun setupDaySelection() {
        val dayViews = mapOf(
            1 to binding.daySun, 2 to binding.dayMon, 3 to binding.dayTue,
            4 to binding.dayWed, 5 to binding.dayThu, 6 to binding.dayFri, 7 to binding.daySat
        )

        dayViews.forEach { (dayIndex, textView) ->
            textView.setOnClickListener {
                toggleDay(dayIndex, textView)
            }
        }
    }

    private fun toggleDay(dayIndex: Int, textView: TextView) {
        if (selectedDays.contains(dayIndex)) {
            selectedDays.remove(dayIndex)
            textView.setBackgroundResource(R.drawable.bg_circle_outline)
            textView.setTextColor(Color.WHITE)
        } else {
            selectedDays.add(dayIndex)
            textView.setBackgroundResource(R.drawable.bg_circle_pink)
            textView.setTextColor(Color.BLACK)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val params = window.attributes
            val displayMetrics = resources.displayMetrics
            params.width = (displayMetrics.widthPixels * 0.97).toInt() // 90% Width
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
    }


}


