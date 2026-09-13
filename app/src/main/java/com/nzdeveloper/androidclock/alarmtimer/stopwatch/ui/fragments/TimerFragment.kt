package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.R
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.FragmentTimerBinding
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.dilaog.SetTimerDialog
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.vm.TimerViewModel
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.ToastUtils
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.applyPinkFocusToAll
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TimerViewModel by viewModels()
    private var typewriterJob: Job? = null
    private val taglineText = "Master Your Productivity"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPresetCards()
        initClickListeners()
        observeViewModel()

        binding.timerContainer.alpha = 0f
        binding.timerContainer.scaleX = 0.8f
        binding.timerContainer.scaleY = 0.8f
        binding.timerContainer
            .animate()
            .alpha(1f).scaleX(1f).scaleY(1f).setDuration(500).start()
    }

    private fun initClickListeners() {
        binding.btnPlayPause.setOnClickListener {
            val currentTime = viewModel.timeLeft.value
            if (currentTime <= 0) {
                ToastUtils.show("Please set a duration first")
                openManualTimerDialog()
                shakeTimerText()
            } else {
                viewModel.togglePauseResume()
            }
        }

        binding.btnReset.setOnClickListener {
            viewModel.resetTimer()
        }

        binding.btnEditTimer.setOnClickListener {
            openManualTimerDialog()
        }

        binding.btnAdd.setOnClickListener {
            viewModel.startTimer(viewModel.timeLeft.value + 60000L)
        }

        // Global pink focus highlight for all interactive Timer elements
        applyPinkFocusToAll(
            binding.btnPlayPause,
            binding.btnReset,
            binding.btnAdd,
            binding.preset5m.root,
            binding.preset25m.root,
            binding.preset15m.root
        )
    }

    private fun openManualTimerDialog() {
        val dialog = SetTimerDialog { selectedMillis ->
            viewModel.totalTimeSet = selectedMillis
            viewModel.startTimer(selectedMillis)
        }
        dialog.show(childFragmentManager, "SetTimerDialog")
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.timeLeft.collect { millis ->
                        updateTimerUI(millis)
                    }
                }
                launch {
                    viewModel.isRunning.collect { isRunning ->
                        val iconRes = if (isRunning) R.drawable.is_play else R.drawable.is_pause
                        _binding?.imgPlayPause?.setImageResource(iconRes)
                    }
                }
            }
        }
    }

    private fun updateTimerUI(millis: Long) {
        val b = _binding ?: return
        val h = (millis / 3600000)
        val m = (millis % 3600000) / 60000
        val s = (millis % 60000) / 1000
        b.tvTimerCountdown.text = String.format("%02d:%02d:%02d", h, m, s)

        if (viewModel.totalTimeSet > 0) {
            val progress = millis.toFloat() / viewModel.totalTimeSet.toFloat()
            b.timerProgressView.progress = progress
        } else {
            b.timerProgressView.progress = 0f
        }
    }

    private fun setupPresetCards() {
        binding.preset5m.apply {
            tvPresetTime.text = "5m"
            tvPresetLabel.text = "Break"
            root.setOnClickListener {
                viewModel.totalTimeSet = 5 * 60 * 1000L
                viewModel.startTimer(5 * 60 * 1000L)
            }
        }
        binding.preset25m.apply {
            tvPresetTime.text = "25m"
            tvPresetLabel.text = "Focus"
            root.setOnClickListener {
                viewModel.totalTimeSet = 25 * 60 * 1000L
                viewModel.startTimer(25 * 60 * 1000L)
            }
        }
        binding.preset15m.apply {
            tvPresetTime.text = "15m"
            tvPresetLabel.text = "Active"
            root.setOnClickListener {
                viewModel.totalTimeSet = 15 * 60 * 1000L
                viewModel.startTimer(15 * 60 * 1000L)
            }
        }
    }

    private fun shakeTimerText() {
        val b = _binding ?: return
        val shake = android.view.animation.AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in)
        b.tvTimerCountdown.startAnimation(shake)
    }

    override fun onResume() {
        super.onResume()
        // Safe static tagline — no append() during measure (crash source removed)
        showTaglineSafe()

        _binding?.timerContainer?.alpha = 0f
        _binding?.timerContainer?.scaleX = 0.8f
        _binding?.timerContainer?.scaleY = 0.8f
        _binding?.timerContainer
            ?.animate()
            ?.alpha(1f)?.scaleX(1f)?.scaleY(1f)?.setDuration(500)?.start()
    }

    private fun showTaglineSafe() {
        val b = _binding ?: return
        // Set full text at once (safe) then fade in — no char-by-char append()
        b.tvTimerTagline.text = taglineText
        b.tvTimerTagline.alpha = 0f
        b.tvTimerTagline.translationY = 10f
        b.tvTimerTagline.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .start()

        // Auto-hide after 4 seconds
        typewriterJob?.cancel()
        typewriterJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(4000)
            _binding?.tvTimerTagline?.animate()
                ?.alpha(0f)
                ?.setDuration(800)
                ?.start()
        }
    }

    override fun onPause() {
        super.onPause()
        typewriterJob?.cancel()
        _binding?.tvTimerTagline?.animate()?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        typewriterJob?.cancel()
        _binding?.tvTimerTagline?.animate()?.cancel()
        _binding = null
    }
}