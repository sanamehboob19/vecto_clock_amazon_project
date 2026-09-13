package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.dilaog


import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.DialogSetTimerBinding

import android.graphics.Color
import android.graphics.drawable.ColorDrawable

class SetTimerDialog(private val onTimeSet: (Long) -> Unit) : DialogFragment() {

    private var _binding: DialogSetTimerBinding? = null
    private val binding get() = _binding!!

    // Track which field is currently selected
    private enum class TimerField { HOUR, MINUTE, SECOND }
    private var currentField = TimerField.MINUTE // Default focus

    // Separate strings for each field
    private var hourStr = ""
    private var minStr = ""
    private var secStr = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogSetTimerBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFieldSelection()
        setupNumberPad()
        updateFocusUI()

        binding.btnDelete.setOnClickListener {
            handleDelete()
        }

        binding.btnCancelTimer.setOnClickListener { dismiss() }

        binding.btnSetTimer.setOnClickListener {
            val totalMillis = calculateFinalMillis()
            if (totalMillis > 0) {
                onTimeSet(totalMillis)
                dismiss()
            }
        }
    }

    private fun setupFieldSelection() {
        // Allow user to tap specific parts of the time to change them
        binding.layoutHour.setOnClickListener {
            currentField = TimerField.HOUR
            updateFocusUI()
        }
        binding.layoutMin.setOnClickListener {
            currentField = TimerField.MINUTE
            updateFocusUI()
        }
        binding.layoutSec.setOnClickListener {
            currentField = TimerField.SECOND
            updateFocusUI()
        }
    }

    private fun setupNumberPad() {
        val listener = View.OnClickListener { v ->
            val digit = (v as Button).text.toString()
            handleInput(digit)
        }

        // Loop through the GridLayout to set listeners on all numeric buttons
        val grid = binding.numPadGrid
        for (i in 0 until grid.childCount) {
            val child = grid.getChildAt(i)
            if (child is Button && child.text.any { it.isDigit() }) {
                child.setOnClickListener(listener)
            }
        }
    }

    private fun handleInput(digit: String) {
        when (currentField) {
            TimerField.HOUR -> if (hourStr.length < 2) hourStr += digit
            TimerField.MINUTE -> if (minStr.length < 2) minStr += digit
            TimerField.SECOND -> if (secStr.length < 2) secStr += digit
        }
        refreshDisplay()
    }

    private fun handleDelete() {
        when (currentField) {
            TimerField.HOUR -> if (hourStr.isNotEmpty()) hourStr = hourStr.dropLast(1)
            TimerField.MINUTE -> if (minStr.isNotEmpty()) minStr = minStr.dropLast(1)
            TimerField.SECOND -> if (secStr.isNotEmpty()) secStr = secStr.dropLast(1)
        }
        refreshDisplay()
    }

    private fun refreshDisplay() {
        binding.tvInputHour.text = hourStr.padStart(2, '0')
        binding.tvInputMin.text = minStr.padStart(2, '0')
        binding.tvInputSec.text = secStr.padStart(2, '0')
    }

    private fun updateFocusUI() {
        val activeColor = Color.parseColor("#FFB6C1") // Your Pink Theme
        val inactiveColor = Color.parseColor("#888888") // Muted Gray

        // Highlight the selected field and dim others
        binding.tvInputHour.setTextColor(if (currentField == TimerField.HOUR) activeColor else inactiveColor)
        binding.tvInputMin.setTextColor(if (currentField == TimerField.MINUTE) activeColor else inactiveColor)
        binding.tvInputSec.setTextColor(if (currentField == TimerField.SECOND) activeColor else inactiveColor)
    }

    private fun calculateFinalMillis(): Long {
        val h = hourStr.toLongOrNull() ?: 0L
        val m = minStr.toLongOrNull() ?: 0L
        val s = secStr.toLongOrNull() ?: 0L

        return (h * 3600 + m * 60 + s) * 1000L
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.CENTER)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}