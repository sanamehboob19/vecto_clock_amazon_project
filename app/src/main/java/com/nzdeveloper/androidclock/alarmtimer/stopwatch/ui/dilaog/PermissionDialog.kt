package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.dilaog


import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.DialogPermissionBinding



class PermissionDialog(private val onAllowClicked: () -> Unit) : DialogFragment() {

    private var _binding: DialogPermissionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPermissionBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAllow.setOnClickListener {
            onAllowClicked()
            dismiss()
        }

        binding.btnNotNow.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val params = window.attributes
            val displayMetrics = resources.displayMetrics
            params.width = (displayMetrics.widthPixels * 0.9).toInt() // 90% Width
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "PermissionDialog"
    }
}