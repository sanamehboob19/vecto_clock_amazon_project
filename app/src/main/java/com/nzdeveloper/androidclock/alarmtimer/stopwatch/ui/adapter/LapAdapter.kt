package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.R
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.Lap

class LapAdapter : RecyclerView.Adapter<LapAdapter.ViewHolder>() {
    private val laps = mutableListOf<Lap>()

    fun addLap(lap: Lap) {
        laps.add(0, lap)
        notifyItemInserted(0)
    }

    fun getLaps(): List<Lap> {
        return laps
    }

    // ---  UPDATE THE LIVE TICKING LAP ---
    fun updateCurrentLapTime(lapTime: String, overallTime: String) {
        if (laps.isNotEmpty()) {
            // Update the data in the list
            laps[0] = laps[0].copy(lapTime = lapTime, overallTime = overallTime)
            // Refresh ONLY the first visible item
            notifyItemChanged(0)
        }
    }

    fun clear() {
        laps.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lap_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = laps[position]
        holder.tvNumber.text = String.format("%02d", item.number)
        holder.tvLapTime.text = item.lapTime
        holder.tvOverallTime.text = item.overallTime
    }

    override fun getItemCount() = laps.size

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvNumber: TextView = v.findViewById(R.id.tv_lap_number)
        val tvLapTime: TextView = v.findViewById(R.id.tv_lap_time)
        val tvOverallTime: TextView = v.findViewById(R.id.tv_overall_time)
    }
}