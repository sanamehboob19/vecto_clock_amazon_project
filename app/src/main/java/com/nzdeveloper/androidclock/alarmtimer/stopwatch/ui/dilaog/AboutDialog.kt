package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.dilaog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.DialogAboutAppBinding

class AboutDialog : DialogFragment() {

    private var _binding: DialogAboutAppBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogAboutAppBinding.inflate(inflater, container, false)

        // Make background transparent to show rounded corners
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCloseAbout.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.let { window ->
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            // Optional: Enter from bottom animation
            window.setWindowAnimations(android.R.style.Animation_Dialog)
        }


//        dialog?.window?.let { window ->
//            val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
////            window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
//            window.setGravity(Gravity.CENTER)
//            // Add smooth fade animation
//            window.attributes.windowAnimations = android.R.style.Animation_Dialog
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}