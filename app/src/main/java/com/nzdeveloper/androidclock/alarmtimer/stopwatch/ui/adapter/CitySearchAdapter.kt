package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.ItemSearchCityBinding
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.WorldClock

class CitySearchAdapter(private val onCitySelected: (WorldClock) -> Unit) :
    RecyclerView.Adapter<CitySearchAdapter.ViewHolder>()
{

    private var allCities = listOf<WorldClock>() // Original List
    private var filteredCities = mutableListOf<WorldClock>() // Display List

    fun setCities(newList: List<WorldClock>) {
        allCities = newList
        filteredCities = newList.toMutableList()
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val cleanQuery = query.lowercase().trim()
        filteredCities.clear()

        if (cleanQuery.isEmpty()) {
            filteredCities.addAll(allCities)
        } else {
            val results = allCities.filter {
                it.cityName.lowercase().contains(cleanQuery) ||
                        it.timeZoneId.lowercase().contains(cleanQuery)
            }
            filteredCities.addAll(results)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchCityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val city = filteredCities[position]
        holder.binding.tvCityName.text = city.cityName
        holder.binding.tvTimezoneId.text = city.timeZoneId
        holder.itemView.setOnClickListener { onCitySelected(city) }
    }

    override fun getItemCount() = filteredCities.size

    class ViewHolder(val binding: ItemSearchCityBinding) : RecyclerView.ViewHolder(binding.root)
}