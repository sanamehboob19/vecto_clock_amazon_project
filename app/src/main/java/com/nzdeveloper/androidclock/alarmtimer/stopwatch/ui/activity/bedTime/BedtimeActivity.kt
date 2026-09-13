package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.activity.bedTime

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.ActivityBedtimeBinding
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.base.BaseActivity
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.vm.BedtimeViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.SleepSchedule
import kotlinx.coroutines.launch
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.applyPinkFocusToAll
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.dilaog.SleepWizardDialog
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.ToastUtils
import java.text.SimpleDateFormat
import java.util.*




@AndroidEntryPoint
class BedtimeActivity :
    BaseActivity<ActivityBedtimeBinding>(ActivityBedtimeBinding::inflate) {
    private val viewModel: BedtimeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnBack.setOnClickListener { finish() }
        binding.btnGetStarted.setOnClickListener { openWizard() }
        binding.cardScheduleSummary.setOnClickListener { openWizard() }
        observeViewModel()
        setupDpadFocus()
    }

    /** Makes Bedtime screen navigable via D-pad remote on Fire TV */
    private fun setupDpadFocus() {
        // Apply pink focus highlight to interactive elements
        applyPinkFocusToAll(binding.btnBack, binding.btnGetStarted)
        binding.cardScheduleSummary.isFocusable = true
        binding.cardScheduleSummary.isFocusableInTouchMode = false
        // Initial focus
        binding.btnBack.post { binding.btnBack.requestFocus() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.schedule.collect { s ->
                    val hasSchedule = s != null
                    binding.layoutIntro.visibility = if (hasSchedule) View.GONE else View.VISIBLE
                    binding.layoutSummary.visibility = if (hasSchedule) View.VISIBLE else View.GONE

                    s?.let { schedule ->
                        // 1. UPDATE CUSTOM WIDGET
                        binding.sleepCircleView.setTimes(
                            schedule.bedtimeHour,
                            schedule.bedtimeMinute,
                            schedule.wakeUpHour,
                            schedule.wakeUpMinute
                        )

                        // 2. UPDATE TIME TEXTS
                        binding.tvDashBedtime.text = formatTime(schedule.bedtimeHour, schedule.bedtimeMinute)
                        binding.tvDashWakeup.text = formatTime(schedule.wakeUpHour, schedule.wakeUpMinute)

                        // 3. DYNAMIC DURATION CALCULATION
                        val totalMinutes = calculateDiff(schedule)
                        val hours = totalMinutes / 60
                        val mins = totalMinutes % 60

                        // Center large text (e.g., "0h 45m" or "8h 00m")
                        binding.tvDashDuration.text = String.format("%dh %02dm", hours, mins)

                        // 4. SMART RECOMMENDATION LOGIC
                        // We check if sleep is less than 7 hours (420 minutes)
                        if (totalMinutes < 420) {
                            binding.tvDashRecommendation.text = "Short Sleep ($hours h sleep fulfillment)"
                            binding.tvDashRecommendation.setTextColor(android.graphics.Color.parseColor("#FF5252")) // Red Warning
                        } else {
                            binding.tvDashRecommendation.text = "Healthy Sleep (Fulfillment reached)"
                            binding.tvDashRecommendation.setTextColor(android.graphics.Color.parseColor("#FFB6C1")) // Pink Primary
                        }

                        // 5. UPDATE FOOTER (Intelligent Day Tracking)
                        binding.tvDashFooter.text = "Next alarm on ${getNextAlarmDay(schedule)}"
                    }
                }
            }
        }
    }

    private fun openWizard() {
        SleepWizardDialog { schedule ->
            viewModel.saveSleepSchedule(this, schedule)

            // SYNC STEP: Tell the OS about this wake-up time
            syncAlarmWithSystem(schedule.wakeUpHour, schedule.wakeUpMinute, schedule.days)

            openSystemBedtimeSettings()


            ToastUtils.show("Syncing with phone settings...")
        } .show(supportFragmentManager, "WIZARD")
    }


    /**
     * Opens the System Digital Wellbeing Bedtime settings
     * Supports Google/Pixel, Samsung, and newer Android standards.
     */
    private fun openSystemBedtimeSettings() {
        val actions = arrayOf(
            "com.google.android.apps.wellbeing.settings.BEDTIME_SETTINGS", // Google/Pixel
            "com.samsung.android.forest.BedtimeSettingsActivity",         // Samsung
            "android.settings.BEDTIME_SETTINGS",                           // Android Standard
            "com.google.android.apps.wellbeing.dashboard.WellbeingDashboardActivity" // Fallback Dashboard
        )

        var success = false
        for (action in actions) {
            try {
                val intent = Intent(action)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                success = true
                break
            } catch (e: Exception) {
                continue
            }
        }

        if (!success) {
            try {
                // If specific page not found, open general Settings
                startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
                ToastUtils.show("Please search for 'Bedtime' in Settings")
            } catch (e: Exception) {
                ToastUtils.show("Could not open system settings.")
            }
        }
    }

    private fun syncAlarmWithSystem(hour: Int, minute: Int, days: List<Int>) {
        try {
            val intent = Intent(android.provider.AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(android.provider.AlarmClock.EXTRA_HOUR, hour)
                putExtra(android.provider.AlarmClock.EXTRA_MINUTES, minute)
                putExtra(android.provider.AlarmClock.EXTRA_MESSAGE, "Vecto Clock: Wake up")

                // Map our days to the System days
                val systemDays = ArrayList<Int>()
                days.forEach { systemDays.add(it) }
                putExtra(android.provider.AlarmClock.EXTRA_DAYS, systemDays)

                putExtra(android.provider.AlarmClock.EXTRA_SKIP_UI, true) // Don't open the clock app
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun checkOverlayPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (!android.provider.Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:$packageName")
                )
                startActivity(intent)
                ToastUtils.show("Please allow 'Display over other apps' to enable Grayscale Mode")
            }
        }
    }


    /**
     * Logic to find the next day the alarm will ring based on selected days
     */
    private fun getNextAlarmDay(s: SleepSchedule): String {
        if (s.days.isEmpty()) return "Not scheduled"

        val now = Calendar.getInstance()
        val today = now.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...

        val alarmTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, s.wakeUpHour)
            set(Calendar.MINUTE, s.wakeUpMinute)
            set(Calendar.SECOND, 0)
        }

        // Check the next 7 days to see which one matches the schedule
        for (i in 0..7) {
            val checkDay = (today + i - 1) % 7 + 1
            if (s.days.contains(checkDay)) {
                // If it's today, check if the time has already passed
                if (i == 0 && alarmTime.after(now)) {
                    return "Today"
                } else if (i == 1) {
                    return "Tomorrow"
                } else if (i > 1) {
                    // Return day name like "Monday"
                    return SimpleDateFormat("EEEE", Locale.getDefault()).format(alarmTime.apply {
                        add(Calendar.DAY_OF_YEAR, i)
                    }.time)
                }
            }
        }
        return "Scheduled"
    }

    /**
     * Formats time to professional 12h AM/PM
     */
    private fun formatTime(h: Int, m: Int): String {
        val amPm = if (h >= 12) "PM" else "AM"
        val dh = when { h == 0 -> 12; h > 12 -> h - 12; else -> h }
        return String.format("%02d:%02d %s", dh, m, amPm)
    }

    /**
     * Calculates total sleep duration in minutes
     */
    private fun calculateDiff(s: SleepSchedule): Int {
        var d = (s.wakeUpHour * 60 + s.wakeUpMinute) - (s.bedtimeHour * 60 + s.bedtimeMinute)
        if (d < 0) d += 1440 // Handles overnight sleep
        return d
    }






}

