package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.fragments


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.FragmentMoreBinding
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.activity.bedTime.BedtimeActivity
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.activity.screenSaver.ScreenSaverActivity
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.activity.widget.WidgetHubActivity
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.activity.worldClock.WorldClockActivity
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.vm.AlarmViewModel
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.ToastUtils
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.applyPinkFocusToAll
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MoreFragment : Fragment() {

    private var _binding: FragmentMoreBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListeners()
    }

    private fun initClickListeners() {
        // Bedtime Mode Card (fix duplicate listener bug)
        binding.cardBedtimeMain.setOnClickListener {
            startActivity(Intent(requireContext(), BedtimeActivity::class.java))
        }
        binding.btnSetupBedtime.setOnClickListener {
            startActivity(Intent(requireContext(), BedtimeActivity::class.java))
        }

        // World Clock Card
        binding.cardWorldClock.setOnClickListener {
            startActivity(Intent(requireContext(), WorldClockActivity::class.java))
        }

        // Screen Saver Card
        binding.cardScreenSaver.setOnClickListener {
            startActivity(Intent(requireContext(), ScreenSaverActivity::class.java))
        }

        // Widget Hub Card
        binding.cardWidgetHub.setOnClickListener {
            startActivity(Intent(requireContext(), WidgetHubActivity::class.java))
            activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Global pink focus highlight for all More Tools cards
        applyPinkFocusToAll(
            binding.cardBedtimeMain,
            binding.cardWorldClock,
            binding.cardScreenSaver,
            binding.cardWidgetHub,
            binding.btnSetupBedtime
        )
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}