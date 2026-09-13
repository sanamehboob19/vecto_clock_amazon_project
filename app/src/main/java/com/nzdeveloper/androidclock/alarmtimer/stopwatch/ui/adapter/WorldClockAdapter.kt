package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.ItemWorldClockBinding
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.WorldClock
import java.text.SimpleDateFormat
import java.util.*

class WorldClockAdapter(private val onDelete: (WorldClock) -> Unit) :
    ListAdapter<WorldClock, WorldClockAdapter.ViewHolder>(WorldDiffCallback())
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWorldClockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(val binding: ItemWorldClockBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(city: WorldClock) {
            binding.tvWorldCityName.text = city.cityName

            // 1. Get Time
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val targetZone = TimeZone.getTimeZone(city.timeZoneId)
            sdf.timeZone = targetZone
            binding.tvWorldTime.text = sdf.format(Date()).replace(" am", "").replace(" pm", "") // Clean numbers only

            // 2. Get Day Status (Today/Tomorrow/Yesterday)
            val localCal = Calendar.getInstance()
            val targetCal = Calendar.getInstance(targetZone)

            val dayDiff = targetCal.get(Calendar.DAY_OF_YEAR) - localCal.get(Calendar.DAY_OF_YEAR)
            binding.tvWorldDayStatus.text = when(dayDiff) {
                1 -> "Tomorrow"
                -1 -> "Yesterday"
                else -> "Today"
            }

            binding.tvWorldTimeDiff.text = getTimeDifference(city.timeZoneId)

            // Long click  delete option
            binding.root.setOnLongClickListener {
                onDelete(city)
                true
            }
        }
    }

    private fun getTimeDifference(targetId: String): String {
        val targetTz = TimeZone.getTimeZone(targetId)
        val localTz = TimeZone.getDefault()

        val diffMillis = targetTz.getOffset(System.currentTimeMillis()) - localTz.getOffset(System.currentTimeMillis())
        val diffHours = diffMillis / (1000 * 60 * 60)

        return when {
            diffHours > 0 -> "$diffHours hours ahead"
            diffHours < 0 -> "${Math.abs(diffHours)} hours behind"
            else -> "Same as local time"
        }
    }
}

class WorldDiffCallback : DiffUtil.ItemCallback<WorldClock>() {
    override fun areItemsTheSame(oldItem: WorldClock, newItem: WorldClock) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: WorldClock, newItem: WorldClock) = oldItem == newItem
}