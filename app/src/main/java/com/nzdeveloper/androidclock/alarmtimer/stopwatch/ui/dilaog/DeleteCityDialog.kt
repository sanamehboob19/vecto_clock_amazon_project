package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.dilaog


import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.DialogDeleteCityBinding
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.WorldClock

class DeleteCityDialog(
    private val city: WorldClock,
    private val onDeleteConfirmed: () -> Unit
) : DialogFragment() {

    private var _binding: DialogDeleteCityBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogDeleteCityBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set dynamic text
        binding.tvDeleteDesc.text = "Are you sure you want to stop tracking time for ${city.cityName}?"

        binding.btnCancelDel.setOnClickListener { dismiss() }

        binding.btnConfirmDel.setOnClickListener {
            onDeleteConfirmed()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.CENTER)
            window.attributes.windowAnimations = android.R.style.Animation_Dialog
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}