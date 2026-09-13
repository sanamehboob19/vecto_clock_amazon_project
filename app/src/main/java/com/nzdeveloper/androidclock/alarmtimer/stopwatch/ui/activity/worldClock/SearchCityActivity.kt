package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.activity.worldClock

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.R

import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.ActivitySearchCityBinding
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.WorldClock
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.adapter.CitySearchAdapter
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.base.BaseActivity
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.vm.WorldClockViewModel
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class SearchCityActivity :
    BaseActivity<ActivitySearchCityBinding>(ActivitySearchCityBinding::inflate) {

    private val viewModel: WorldClockViewModel by viewModels()
    private lateinit var searchAdapter: CitySearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupRecyclerView()
        loadAllCities()
        setupSearchLogic()
    }

    private fun setupRecyclerView() {
        searchAdapter = CitySearchAdapter { selectedCity ->
            viewModel.addCity(selectedCity)
            finish()
        }
        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(this@SearchCityActivity)
            adapter = searchAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadAllCities() {
        val tzIds = TimeZone.getAvailableIDs()
        val cityList = mutableListOf<WorldClock>()

        for (id in tzIds) {
            if (id.contains("/")) {
                // "America/Los_Angeles" -> parts = ["America", "Los_Angeles"]
                val parts = id.split("/")
                val region = parts[0].replace("_", " ") // America
                val city = parts[1].replace("_", " ")  // Los Angeles

                cityList.add(WorldClock(
                    cityName = "$city ($region)",
                    timeZoneId = id
                ))
            }
        }

        val sortedList = cityList.distinctBy { it.timeZoneId }.sortedBy { it.cityName }
        searchAdapter.setCities(sortedList)
    }

    private fun setupSearchLogic() {
        binding.etSearchCity.doAfterTextChanged { text ->
            searchAdapter.filter(text.toString())
        }
    }
}