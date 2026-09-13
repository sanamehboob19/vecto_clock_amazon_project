package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.ItemAlarmBinding
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.extension.getActivity
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.Alarm
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.dilaog.EditAlarmBottomSheet
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.R


class AlarmAdapter(
    private val onToggle: (Alarm, Boolean) -> Unit,
    private val onUpdate: (Alarm) -> Unit,
    private val onDelete: (Alarm) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>()
{

    // 1. Manual list to hold data
    private var alarmList = listOf<Alarm>()

    // 2. Function to update the list manually
    fun updateData(newList: List<Alarm>) {
        this.alarmList = newList
        notifyDataSetChanged() // This forces the UI to refresh every single time
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = ItemAlarmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(alarmList[position])
    }

    override fun getItemCount(): Int = alarmList.size

    inner class AlarmViewHolder(private val binding: ItemAlarmBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(alarm: Alarm) {
            val context = binding.root.context

            // 1. Reset the Switch listener to prevent bugs while scrolling
            binding.switchAlarm.setOnCheckedChangeListener(null)

            // 2. Set Time and Label
            val displayHour = when {
                alarm.hour == 0 -> 12
                alarm.hour > 12 -> alarm.hour - 12
                else -> alarm.hour
            }
            binding.tvAlarmTime.text = String.format("%d:%02d", displayHour, alarm.minute)
            binding.tvAlarmAmPm.text = alarm.amPm.lowercase()
            binding.tvAlarmLabel.text = alarm.label


            // 2. THEME COLORS
            val pinkColor = ContextCompat.getColor(context, R.color.pinkPrimary)
            val greyBgColor = Color.parseColor("#2C2C2C") // Modern Dark Grey
            val darkTextColor = Color.BLACK
            val lightTextColor = Color.WHITE
            val mutedLightText = Color.parseColor("#D1D1D1")
            val mutedDarkText = Color.parseColor("#444444")

            if (alarm.isEnabled) {
                // --- SELECTED STATE (Pink) ---
                binding.cardRoot.setCardBackgroundColor(pinkColor)

                binding.tvAlarmTime.setTextColor(darkTextColor)
                binding.tvAlarmAmPm.setTextColor(darkTextColor)
                binding.tvAlarmLabel.setTextColor(mutedDarkText)
                binding.tvStatusInfo.setTextColor(mutedDarkText)

                binding.tvStatusInfo.text = if (alarm.days.isEmpty()) "Today" else formatDays(alarm.days)
                binding.switchAlarm.trackTintList = ColorStateList.valueOf(Color.parseColor("#000000")) // White track on pink
            } else {
                // --- UNSELECTED STATE (Grey) ---
                binding.cardRoot.setCardBackgroundColor(greyBgColor)

                binding.tvAlarmTime.setTextColor(mutedLightText)
                binding.tvAlarmAmPm.setTextColor(mutedLightText)
                binding.tvAlarmLabel.setTextColor(Color.GRAY)
                binding.tvStatusInfo.setTextColor(Color.GRAY)

                binding.tvStatusInfo.text = "Not scheduled"
//                binding.tvDismissBtn.visibility = View.GONE
                binding.switchAlarm.trackTintList = ColorStateList.valueOf(Color.parseColor("#444444")) // Grey track
            }


            // 4. Set Switch State
            binding.switchAlarm.isChecked = alarm.isEnabled

            // 5. Click Listeners
            binding.switchAlarm.setOnCheckedChangeListener { _, isChecked ->
                onToggle(alarm, isChecked)
            }

            binding.root.setOnClickListener {
                val activity = context.getActivity()
                if (activity != null) {
                    val bottomSheet = EditAlarmBottomSheet(
                        alarm = alarm,
                        onSave = { updatedAlarm -> onUpdate(updatedAlarm) },
                        onDelete = { alarmToDelete -> onDelete(alarmToDelete) }
                    )
                    bottomSheet.show(activity.supportFragmentManager, "EditAlarm")
                }
            }
        }


    }

    private fun formatDays(days: List<Int>): String {
        if (days.isEmpty()) return "Once"
        if (days.size == 7) return "Every day"
        val dayNames = listOf("", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        return days.sorted().joinToString(", ") { dayNames[it] }
    }

}