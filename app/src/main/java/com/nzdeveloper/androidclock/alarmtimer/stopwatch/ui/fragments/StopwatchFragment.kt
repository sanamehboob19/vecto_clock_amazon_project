package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.R
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.FragmentStopwatchBinding
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.Lap
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.adapter.LapAdapter
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.base.BaseFragment
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.TinyDB
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.graphics.Color
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.applyPinkFocusToAll


@AndroidEntryPoint
class StopwatchFragment :
    BaseFragment<FragmentStopwatchBinding>(FragmentStopwatchBinding::inflate) {

    @Inject lateinit var tinyDB: TinyDB

    private var isRunning = false
    private var isStarted = false
    private var timeInMs = 0L
    private var startTime = 0L
    private var lastLapTime = 0L
    private var lapCount = 0

    private var handler = Handler(Looper.getMainLooper())
    private lateinit var lapAdapter: LapAdapter

    private val runnable = object : Runnable {
        override fun run() {
            timeInMs = SystemClock.elapsedRealtime() - startTime
            val formattedTotalTime = formatTime(timeInMs)

            binding.tvStopwatchTime.text = formattedTotalTime

            if (isRunning && lapAdapter.itemCount > 0) {
                val liveLapMs = timeInMs - lastLapTime
                lapAdapter.updateCurrentLapTime(formatTime(liveLapMs), formattedTotalTime)
            }

            handler.postDelayed(this, 30)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadSavedState()
        updateLapListVisibility()

        binding.btnStartStop.setOnClickListener {
            if (isRunning) pauseStopwatch() else startStopwatch()
        }

        binding.btnLap.setOnClickListener {
            recordLap()
        }

        binding.btnReset.setOnClickListener {
            resetStopwatch()
        }

        applyPinkFocusToAll(
            binding.btnStartStop,
            binding.btnReset,
            binding.btnLap
        )

        // NOTE: removed the binding.btnStartStop.post { requestFocus() } call
        // that used to be here. MainActivity.focusActiveFragmentContent()
        // is now the single owner of "which view gets focus when this tab
        // opens" — having both fight over requestFocus() was part of the
        // flicker/stuck-on-Settings bug.
    }

    private fun setupRecyclerView() {
        lapAdapter = LapAdapter()
        binding.rvLaps.apply {
            adapter = lapAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
        }

        updateLapListVisibility()
    }

    private fun startStopwatch() {
        startTime = SystemClock.elapsedRealtime() - timeInMs

        if (lapAdapter.itemCount == 0) {
            lapCount = 1
            lastLapTime = 0L
            lapAdapter.addLap(Lap(lapCount, "00:00.00", "00:00.00"))
        }

        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, 0)
        isRunning = true
        isStarted = true
        updateButtonUI()
    }

    private fun pauseStopwatch() {
        handler.removeCallbacks(runnable)
        isRunning = false
        updateButtonUI()
        saveState()
    }

    private fun recordLap() {
        lapCount++
        lastLapTime = timeInMs

        val newLap = Lap(lapCount, "00:00.00", formatTime(timeInMs))
        lapAdapter.addLap(newLap)

        binding.rvLaps.scrollToPosition(0)
        saveState()
        updateLapListVisibility()
    }

    private fun resetStopwatch() {
        handler.removeCallbacks(runnable)
        isRunning = false
        isStarted = false
        timeInMs = 0L
        lastLapTime = 0L
        lapCount = 0

        binding.tvStopwatchTime.text = "00:00.00"
        lapAdapter.clear()

        updateButtonUI()
        tinyDB.remove("stopwatch_data")
        updateLapListVisibility()
    }

    private fun updateButtonUI() {
        if (!isStarted) {
            binding.btnStartStop.text = "Start"
            binding.btnStartStop.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pinkPrimary))
            binding.layoutSecondaryActions.visibility = View.GONE
        } else {
            binding.layoutSecondaryActions.visibility = View.VISIBLE
            if (isRunning) {
                binding.btnStartStop.text = "Stop"
                binding.btnStartStop.setBackgroundColor(Color.parseColor("#4DFF5252"))
                binding.btnLap.visibility = View.VISIBLE
            } else {
                binding.btnStartStop.text = "Resume"
                binding.btnStartStop.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pinkPrimary))
                binding.btnLap.visibility = View.GONE
            }
        }
    }

    private fun formatTime(millis: Long): String {
        val safeMillis = if (millis < 0) 0L else millis // guard against negative time
        val mins = (safeMillis / 60000)
        val secs = (safeMillis % 60000) / 1000
        val hunds = (safeMillis % 1000) / 10
        return String.format("%02d:%02d.%02d", mins, secs, hunds)
    }

    private fun saveState() {
        val data = mutableMapOf<String, Any>()
        data["timeInMs"] = timeInMs
        data["isRunning"] = isRunning
        data["isStarted"] = isStarted
        data["lastLapTime"] = lastLapTime
        data["lapCount"] = lapCount
        // startTime intentionally NOT saved — it's boot-relative
        // (SystemClock.elapsedRealtime) and meaningless across reboots.
        // It's always recomputed fresh from timeInMs on load below.

        val lapsJson = Gson().toJson(lapAdapter.getLaps())
        data["laps"] = lapsJson

        tinyDB.putString("stopwatch_data", Gson().toJson(data))
    }

    private fun loadSavedState() {
        val json = tinyDB.getString("stopwatch_data")
        if (json.isEmpty()) return

        try {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val data: Map<String, Any>? = Gson().fromJson(json, type)

            if (data == null) return

            timeInMs = (data["timeInMs"] as? Double ?: 0.0).toLong()
            if (timeInMs < 0) timeInMs = 0L // guard against corrupted/negative saved value

            isStarted = data["isStarted"] as? Boolean ?: false
            lastLapTime = (data["lastLapTime"] as? Double ?: 0.0).toLong()
            lapCount = (data["lapCount"] as? Double ?: 0.0).toInt()

            val lapsJson = data["laps"] as? String
            if (!lapsJson.isNullOrEmpty()) {
                val lapListType = object : TypeToken<List<Lap>>() {}.type
                val savedLaps: List<Lap> = Gson().fromJson(lapsJson, lapListType)

                lapAdapter.clear()
                savedLaps.reversed().forEach { lapAdapter.addLap(it) }
            }

            isRunning = data["isRunning"] as? Boolean ?: false
            if (isRunning) {
                // FIX: always recompute startTime fresh from timeInMs instead
                // of trusting a saved startTime. SystemClock.elapsedRealtime()
                // resets on every device/emulator reboot, so an old saved
                // startTime from a previous boot session produces a huge
                // negative "timeInMs" (e.g. the -82:00.-30 bug) on the very
                // first tick after loading.
                startTime = SystemClock.elapsedRealtime() - timeInMs

                handler.postDelayed(runnable, 0)
                updateButtonUI()
            } else {
                binding.tvStopwatchTime.text = formatTime(timeInMs)
                updateButtonUI()
            }

        } catch (e: Exception) {
            tinyDB.remove("stopwatch_data")
            e.printStackTrace()
        }

        updateLapListVisibility()
    }

    override fun onPause() {
        super.onPause()
        if (isStarted) saveState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(runnable)
    }

    private fun updateLapListVisibility() {
        if (::lapAdapter.isInitialized) {
            if (lapAdapter.itemCount == 0) {
                binding.tvLapHint.visibility = View.VISIBLE
                binding.rvLaps.visibility = View.GONE
            } else {
                binding.tvLapHint.visibility = View.GONE
                binding.rvLaps.visibility = View.VISIBLE
            }
        }
    }
}






