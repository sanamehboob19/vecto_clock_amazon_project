package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.dilaog

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.R
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.LayoutEditAlarmBinding
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.Alarm
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


class EditAlarmBottomSheet(
    private val alarm: Alarm,
    private val onSave: (Alarm) -> Unit,
    private val onDelete: (Alarm) -> Unit
) : BottomSheetDialogFragment()
{

    private var selectedRingtoneUri: String? = alarm.ringtoneUri

    // This launches the system ringtone picker
    private val ringtonePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (uri != null) {
                selectedRingtoneUri = uri.toString()
                updateRingtoneNameUI(uri)
            }
        }
    }

    private var _binding: LayoutEditAlarmBinding? = null
    private val binding get() = _binding!!

    // Local state to track changes before saving
    private var selectedHour: Int = alarm.hour
    private var selectedMinute: Int = alarm.minute
    private val selectedDays = alarm.days.toMutableSet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LayoutEditAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Initial UI Setup from the passed 'alarm' object
        binding.etAlarmLabel.setText(alarm.label)
        binding.switchVibrateEdit.isChecked = alarm.isVibrate
        updateTimeText(selectedHour, selectedMinute)
        setupDaySelection()

        // 2. IMPORTANT: Calculate "Rings in..." immediately on open
        calculateRingingTime()

        // 3. Edit Time Button
        binding.btnChangeTime.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(selectedHour)
                .setMinute(selectedMinute)
                .setTitleText("Select Time")
                .build()

            picker.addOnPositiveButtonClickListener {
                selectedHour = picker.hour
                selectedMinute = picker.minute
                updateTimeText(selectedHour, selectedMinute)
                calculateRingingTime() // Recalculate after time change
            }
            picker.show(childFragmentManager, "TIME_PICKER")
        }

        // 4. Save Button
        binding.btnSaveEdit.setOnClickListener {
            val updatedAlarm = alarm.copy(
                hour = selectedHour,
                minute = selectedMinute,
                amPm = if (selectedHour >= 12) "PM" else "AM",
                label = binding.etAlarmLabel.text.toString().ifEmpty { "Alarm" },
                isVibrate = binding.switchVibrateEdit.isChecked,
                days = selectedDays.toList(),
                isEnabled = true, // Re-enable if it was off
                ringtoneUri = selectedRingtoneUri

            )
            onSave(updatedAlarm)
            dismiss()
        }

        // 5. Delete Button
        binding.btnDeleteEdit.setOnClickListener {
            onDelete(alarm)
            dismiss()
        }

        binding.rowRingtone.setOnClickListener {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Sound")
                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedRingtoneUri?.let { Uri.parse(it) })
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            }
            ringtonePickerLauncher.launch(intent)
        }
        selectedRingtoneUri?.let { updateRingtoneNameUI(Uri.parse(it)) }


    }

    private fun updateRingtoneNameUI(uri: Uri) {
        val ringtone = RingtoneManager.getRingtone(requireContext(), uri)
        val name = ringtone.getTitle(requireContext())
        binding.tvRingtoneName.text = name
    }


    private fun calculateRingingTime() {
        // 1. Check if the alarm is currently disabled in the database
        // If it's OFF, we shouldn't show "Rings Tomorrow"
        if (!alarm.isEnabled) {
            binding.tvUpcomingInfo.text = "Alarm is currently inactive"
            binding.tvUpcomingInfo.setTextColor(Color.parseColor("#FF5252")) // Red color to show it's OFF
            return
        }

        val now = Calendar.getInstance()
        val alarmTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var daysUntilNext = 0

        if (selectedDays.isNotEmpty()) {
            // Repeating logic
            val todayOfWeek = now.get(Calendar.DAY_OF_WEEK)
            daysUntilNext = -1

            for (i in 0..7) {
                val checkDay = (todayOfWeek + i - 1) % 7 + 1
                if (selectedDays.contains(checkDay)) {
                    // Check if time passed today
                    if (i == 0 && alarmTime.timeInMillis <= now.timeInMillis) continue

                    daysUntilNext = i
                    break
                }
            }
        } else {
            // One-time logic
            if (alarmTime.timeInMillis <= now.timeInMillis) {
                daysUntilNext = 1
            }
        }

        // 2. Final UI Update
        if (daysUntilNext != -1) {
            alarmTime.add(Calendar.DAY_OF_MONTH, daysUntilNext)

            val diff = alarmTime.timeInMillis - now.timeInMillis
            val hours = diff / (1000 * 60 * 60)
            val minutes = (diff / (1000 * 60)) % 60

            val dayName = when (daysUntilNext) {
                0 -> "Today"
                1 -> "Tomorrow"
                else -> SimpleDateFormat("EEEE", Locale.getDefault()).format(alarmTime.time)
            }

            binding.tvUpcomingInfo.text = "Upcoming: $dayName ($hours h $minutes m)"
            binding.tvUpcomingInfo.setTextColor(Color.parseColor("#888888")) // Muted Gray
        } else {
            binding.tvUpcomingInfo.text = "No days selected"
            binding.tvUpcomingInfo.setTextColor(Color.parseColor("#FF5252"))
        }
    }

    private fun setupDaySelection() {
        val daysMap = mapOf(
            2 to binding.dayM, 3 to binding.dayT, 4 to binding.dayW,
            5 to binding.dayTh, 6 to binding.dayF, 7 to binding.dayS, 1 to binding.daySu
        )

        daysMap.forEach { (index, view) ->
            updateDayCircleUI(view, selectedDays.contains(index))

            view.setOnClickListener {
                if (selectedDays.contains(index)) selectedDays.remove(index)
                else selectedDays.add(index)

                updateDayCircleUI(view, selectedDays.contains(index))
                calculateRingingTime() // Recalculate after day change
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

    private fun updateTimeText(h: Int, m: Int) {
        val displayHour = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        val amPm = if (h >= 12) "PM" else "AM"
        binding.tvEditTime.text = String.format("%02d:%02d %s", displayHour, m, amPm)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
