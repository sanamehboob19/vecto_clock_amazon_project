package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.activity.worldClock

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.R


import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.ActivityWorldClockBinding
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.WorldClock
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.adapter.WorldClockAdapter
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.base.BaseActivity
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.dilaog.DeleteCityDialog
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.vm.WorldClockViewModel
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.applyPinkFocusToAll
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.ToastUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WorldClockActivity : BaseActivity<ActivityWorldClockBinding>(ActivityWorldClockBinding::inflate) {

    private val viewModel: WorldClockViewModel by viewModels()
    private lateinit var worldAdapter: WorldClockAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupRecyclerView()
        observeCities()
        binding.btnBack.setOnClickListener { finish() }
        binding.fabAddCity.setOnClickListener {
            startActivity(Intent(this, SearchCityActivity::class.java))
        }
        // D-pad focus for Fire TV
        applyPinkFocusToAll(binding.btnBack)
        binding.fabAddCity.isFocusable = true
        binding.fabAddCity.isFocusableInTouchMode = false
        binding.btnBack.post { binding.btnBack.requestFocus() }
    }

    private fun setupRecyclerView() {
        worldAdapter = WorldClockAdapter { cityToDelete ->
            showDeleteConfirmation(cityToDelete)
        }
        binding.rvWorldClocks.apply {
            layoutManager = LinearLayoutManager(this@WorldClockActivity)
            adapter = worldAdapter
        }
    }

    private fun observeCities() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allCities.collect { list ->
                    if (list.isEmpty()) {
                        binding.layoutEmptyWorld.visibility = View.VISIBLE
                        binding.rvWorldClocks.visibility = View.GONE
                    } else {
                        binding.layoutEmptyWorld.visibility = View.GONE
                        binding.rvWorldClocks.visibility = View.VISIBLE
                        worldAdapter.submitList(list)
                    }
                }
            }
        }
    }

    private fun showDeleteConfirmation(city: WorldClock) {
        val dialog = DeleteCityDialog(city) {
            // This runs when user clicks "Remove"
            viewModel.deleteCity(city)
            ToastUtils.show("${city.cityName} removed")
        }
        dialog.show(supportFragmentManager, "DeleteCityDialog")
    }
}