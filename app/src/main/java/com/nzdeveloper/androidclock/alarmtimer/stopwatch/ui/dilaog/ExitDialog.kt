package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.dilaog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.DialogExitAppBinding

class ExitDialog : DialogFragment() {

    private var _binding: DialogExitAppBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogExitAppBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnStay.setOnClickListener { dismiss() }

        binding.btnExit.setOnClickListener {
            activity?.finishAffinity() // Closes all activities and exits app
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val params = window.attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT

            params.gravity = Gravity.BOTTOM

            // Slide up animation
            window.attributes.windowAnimations = android.R.style.Animation_InputMethod
            window.attributes = params
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}