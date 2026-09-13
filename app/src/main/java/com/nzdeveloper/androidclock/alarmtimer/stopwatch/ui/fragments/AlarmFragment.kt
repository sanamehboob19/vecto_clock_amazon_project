package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.AlarmManager
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.R
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.DialogPermissionBinding
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.FragmentAlarmBinding
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.Alarm
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.service.AlarmScheduler
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.adapter.AlarmAdapter
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.dilaog.AddAlarmDialog
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.dilaog.PermissionDialog
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.vm.AlarmViewModel
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.DeviceUtils
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.ToastUtils
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
@AndroidEntryPoint
class AlarmFragment : Fragment() {

    private var _binding: FragmentAlarmBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AlarmViewModel by viewModels()
    private lateinit var alarmAdapter: AlarmAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ── Fire TV Check ────────────────────────────────────
        // Alarm setting requires touch input and system alarm permissions
        // that are not available on Fire TV. Show informational overlay.
        if (DeviceUtils.isFireTV(requireContext())) {
            showFireTvNotSupportedOverlay()
            return
        }
        // ──────────────────────────────────────────────

        setupRecyclerView()
        observeAlarms()
        startEmptyStateAnimation()

        parentFragmentManager.setFragmentResultListener("alarm_refresh", viewLifecycleOwner) { _, bundle ->
            val updated = bundle.getBoolean("updated")
            if (updated) {
                // Flow collector already auto-updates — just notify for immediate visual refresh
                alarmAdapter.notifyDataSetChanged()
            }
        }

        binding.fabAddAlarm.setOnClickListener {
            // Check permissions before allowing user to set an alarm
            if (isNotificationPermissionGranted() && isExactAlarmPermissionGranted()) {
                openTimeSetterDialog()
            } else {
                showPermissionExplanation()
            }
        }
    }

    /**
     * Shows a beautiful "Not supported on TV" overlay when app runs on Fire TV.
     * The alarm feature requires touch input and system permissions not available on TV.
     */
    private fun showFireTvNotSupportedOverlay() {
        // Hide all original alarm UI
        binding.fabAddAlarm.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.GONE
        binding.rvAlarms.visibility = View.GONE

        // Create overlay programmatically
        val overlay = FrameLayout(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(android.graphics.Color.parseColor("#0F1113"))
        }

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // TV icon
        val icon = ImageView(requireContext()).apply {
            setImageResource(R.drawable.ic_alarm)
            layoutParams = LinearLayout.LayoutParams(160, 160).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
            alpha = 0.3f
            // Tint pink
            setColorFilter(android.graphics.Color.parseColor("#FFB6C1"))
        }

        // Title
        val title = TextView(requireContext()).apply {
            text = "📺 Not Supported on TV"
            setTextColor(android.graphics.Color.WHITE)
            textSize = 22f
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 0)
        }

        // Subtitle
        val subtitle = TextView(requireContext()).apply {
            text = "Alarm scheduling requires a smartphone or tablet.\nOpen VectoClock on your phone to set alarms."
            setTextColor(android.graphics.Color.parseColor("#888888"))
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(60, 16, 60, 0)
            setLineSpacing(6f, 1f)
        }

        container.addView(icon)
        container.addView(title)
        container.addView(subtitle)
        overlay.addView(container)

        // Add overlay to fragment root
        binding.root.addView(overlay)
    }

    /**
     * Professional Flow: We check permissions again on Resume.
     * If the user just came back from the 'Alarms & Reminders' screen,
     * this ensures the app is ready.
     */
    override fun onResume() {
        super.onResume()
        if (isNotificationPermissionGranted() && isExactAlarmPermissionGranted()) {
            // Optional: You could auto-open the dialog here if you saved a 'pending' state
        }
    }

    private fun setupRecyclerView() {
        alarmAdapter = AlarmAdapter(
            onToggle = { alarm, isEnabled ->
                viewModel.toggleAlarm(requireContext(), alarm, isEnabled)
            },
            onUpdate = { updatedAlarm ->
                viewModel.updateAlarm(requireContext(), updatedAlarm)
                ToastUtils.show("Alarm Updated")
            },
            onDelete = { alarmToDelete ->
                viewModel.deleteAlarm(requireContext(), alarmToDelete)
                ToastUtils.show("Alarm Deleted")
            }
        )

        binding.rvAlarms.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = alarmAdapter
        }
    }

    private fun observeAlarms() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allAlarms.collect { list ->
                    // Use the new manual update function
                    alarmAdapter.updateData(list)

                    if (list.isEmpty()) {
                        binding.layoutEmptyState.visibility = View.VISIBLE
                        binding.rvAlarms.visibility = View.GONE
                    } else {
                        binding.layoutEmptyState.visibility = View.GONE
                        binding.rvAlarms.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun showPermissionExplanation() {
        val dialog = PermissionDialog {
            checkAndRequestPermissions()
        }
        dialog.show(childFragmentManager, PermissionDialog.TAG)
    }

    private fun checkAndRequestPermissions() {
        // 1. Check Notification (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }

        // 2. Check Exact Alarm (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                try {
                    val intent = Intent().apply {
                        action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                        data = Uri.fromParts("package", requireContext().packageName, null)
                    }
                    startActivity(intent)
                    ToastUtils.show("Please enable 'Alarms & Reminders'")
                } catch (e: Exception) {
                    ToastUtils.show("Could not open settings. Please enable manually.")
                }
                return
            }
        }

        // 3. If everything is granted
        openTimeSetterDialog()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // No matter if they allowed or denied notifications, we check the next step (Exact Alarm)
        checkAndRequestPermissions()
    }

    private fun openTimeSetterDialog() {
        // 1. Build the Material Time Picker
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(7) // Default hour
            .setMinute(0) // Default minute
            .setTitleText("Set Alarm")
            .build()

        // 2. Handle the "OK" button click
        picker.addOnPositiveButtonClickListener {
            val hour24 = picker.hour
            val minute = picker.minute

            // 3. Create a new Alarm with default settings
            // User can edit days/label later from the Bottom Sheet
            val newAlarm = Alarm(
                hour = hour24,
                minute = minute,
                amPm = if (hour24 >= 12) "PM" else "AM",
                days = emptyList(), // Initially no days selected
                isEnabled = true,
                label = "Alarm",
                isVibrate = true
            )

            // 4. Save to Database and Schedule
            viewModel.addAlarm(requireContext(), newAlarm)

            ToastUtils.show("Alarm set. Tap to customize.")
        }

        picker.show(childFragmentManager, "FAB_TIME_PICKER")
    }

    private fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    private fun isExactAlarmPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else true
    }

    override fun onDestroyView() {
        // Cancel infinite animator to prevent NPE crash after fragment is destroyed
        emptyStateAnimator?.cancel()
        emptyStateAnimator = null
        super.onDestroyView()
        _binding = null
    }


    // Stored reference so we can cancel it in onDestroyView to prevent NPE crash
    private var emptyStateAnimator: AnimatorSet? = null

    private fun startEmptyStateAnimation() {
        val scaleUpX = ObjectAnimator.ofFloat(binding.ivEmptyIcon, "scaleX", 1f, 1.1f)
        val scaleUpY = ObjectAnimator.ofFloat(binding.ivEmptyIcon, "scaleY", 1f, 1.1f)

        scaleUpX.duration = 2000
        scaleUpY.duration = 2000

        scaleUpX.repeatCount = ObjectAnimator.INFINITE
        scaleUpX.repeatMode = ObjectAnimator.REVERSE
        scaleUpY.repeatCount = ObjectAnimator.INFINITE
        scaleUpY.repeatMode = ObjectAnimator.REVERSE

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleUpX, scaleUpY)
        emptyStateAnimator = animatorSet
        animatorSet.start()
    }

}