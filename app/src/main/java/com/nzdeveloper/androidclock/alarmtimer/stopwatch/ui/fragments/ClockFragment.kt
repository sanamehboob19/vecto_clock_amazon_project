package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.R
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.FragmentClockBinding
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.vm.ClockViewModel
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.applyPinkFocusToAll
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.DeviceUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle


@AndroidEntryPoint
class ClockFragment : Fragment() {

    private var _binding: FragmentClockBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ClockViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentClockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDate()
        initClickListeners()
        observeViewModel()
    }

    private fun setupDate() {
        // This format results in: MONDAY, OCT 23
        val datePattern = "EEEE, MMM dd"

        // Applying the pattern to the TextClock
        binding.dateClock.format12Hour = datePattern
        binding.dateClock.format24Hour = datePattern

        // Optional: Make it uppercase to match your design
        binding.dateClock.isAllCaps = true

    }

    private fun initClickListeners() {
        binding.btnDigital.setOnClickListener { viewModel.setClockMode(true) }
        binding.btnClassic.setOnClickListener { viewModel.setClockMode(false) }

        // Global pink focus highlight for Clock toggle buttons
        applyPinkFocusToAll(binding.btnDigital, binding.btnClassic)

        // On TV: auto-focus Digital button when Clock tab is selected
        if (DeviceUtils.needsDpadNavigation(requireContext())) {
            binding.btnDigital.post { binding.btnDigital.requestFocus() }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isDigitalMode.collect { isDigital ->
                    handleClockSwitchWithAnimation(isDigital)
                    updateToggleUI(isDigital)
                }
            }
        }
    }

    private fun handleClockSwitchWithAnimation(isDigital: Boolean) {
        val tagline = if (isDigital) "Precision in every second" else "Timeless Classic Elegance"

        // 1. Tagline Animation (Slide out, change text, slide in)
        binding.tvClockTagline.animate().alpha(0f).translationY(-20f).setDuration(200).withEndAction {
            binding.tvClockTagline.text = tagline.uppercase()
            binding.tvClockTagline.animate().alpha(1f).translationY(0f).setDuration(400).start()
        }.start()

        // 2. Clock Cross-fade
        if (isDigital) {
            binding.layoutDigitalClock.visibility = View.VISIBLE
            binding.layoutDigitalClock.alpha = 0f
            binding.layoutDigitalClock.animate().alpha(1f).setDuration(500).start()
            binding.layoutClassicClock.visibility = View.GONE
        } else {
            binding.layoutClassicClock.visibility = View.VISIBLE
            binding.layoutClassicClock.alpha = 0f
            binding.layoutClassicClock.animate().alpha(1f).setDuration(500).start()
            binding.layoutDigitalClock.visibility = View.GONE
        }

        // 3. Aura Breathing Animation
        binding.viewAuraGlow?.animate()?.scaleX(1.2f)?.scaleY(1.2f)?.setDuration(300)?.withEndAction {
            binding.viewAuraGlow?.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(300)?.start()
        }?.start()
    }

    private fun updateToggleUI(isDigital: Boolean) {
        if (isDigital) {
            binding.btnDigital.setBackgroundResource(R.drawable.bg_pill_pink)
            binding.btnDigital.setTextColor(Color.BLACK)
            binding.btnClassic.setBackgroundResource(0)
            binding.btnClassic.setTextColor(Color.WHITE)
        } else {
            binding.btnClassic.setBackgroundResource(R.drawable.bg_pill_pink)
            binding.btnClassic.setTextColor(Color.BLACK)
            binding.btnDigital.setBackgroundResource(0)
            binding.btnDigital.setTextColor(Color.WHITE)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}