package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.dilaog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.R
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.DialogDismissMethodBinding
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.DeviceUtils
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.TinyDB
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DismissMethodDialog(private val onSave: (String) -> Unit) : DialogFragment() {

    private var _binding: DialogDismissMethodBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var tinyDB: TinyDB

    private var selectedMethod = "Swipe"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDismissMethodBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load saved preference
        selectedMethod = tinyDB.getString("dismiss_method", "Swipe")

        initializeSelectionCards()
        updateVisualStates()

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnOk.setOnClickListener {
            tinyDB.putString("dismiss_method", selectedMethod)
            onSave(selectedMethod)
            dismiss()
        }
    }

    private fun initializeSelectionCards() {
        // Setup Tap Card
        binding.choiceTap.apply {
            tvChoiceLabel.text = "Tap Mode"
            tvChoiceDesc.text = "Touch buttons to dismiss"
            ivChoiceIcon.setImageResource(R.drawable.ic_tap_gesture)
            miniTapLayout.visibility = View.VISIBLE
            miniSwipeLayout.visibility = View.GONE

            cardRoot.setOnClickListener {
                selectedMethod = "Tap"
                updateVisualStates()
            }
        }

        // Setup Swipe Card
        binding.choiceSwipe.apply {
            tvChoiceLabel.text = "Swipe Mode"
            tvChoiceDesc.text = "Slide thumb to dismiss"
            ivChoiceIcon.setImageResource(R.drawable.ic_swipe_gesture)
            miniSwipeLayout.visibility = View.VISIBLE
            miniTapLayout.visibility = View.GONE

            cardRoot.setOnClickListener {
                selectedMethod = "Swipe"
                updateVisualStates()
            }
        }

        // D-Pad focus handling for TV / Remote navigation
        if (DeviceUtils.needsDpadNavigation(requireContext())) {
            binding.choiceTap.cardRoot.nextFocusRightId = binding.choiceSwipe.cardRoot.id
            binding.choiceTap.cardRoot.nextFocusDownId = binding.btnOk.id

            binding.choiceSwipe.cardRoot.nextFocusLeftId = binding.choiceTap.cardRoot.id
            binding.choiceSwipe.cardRoot.nextFocusDownId = binding.btnOk.id

            binding.btnCancel.nextFocusRightId = binding.btnOk.id
            binding.btnCancel.nextFocusUpId = binding.choiceTap.cardRoot.id

            binding.btnOk.nextFocusLeftId = binding.btnCancel.id
            binding.btnOk.nextFocusUpId = binding.choiceSwipe.cardRoot.id
        }
    }

    /**
     * Updates the Live Preview area and card selection highlights
     */
    private fun updateVisualStates() {
        val pink = ContextCompat.getColor(requireContext(), R.color.pinkPrimary)
        val defaultStroke = Color.parseColor("#2C2E3A")

        // 1. Update Large Live Preview Area
        if (selectedMethod == "Tap") {
            binding.previewTapLayout.visibility = View.VISIBLE
            binding.previewSwipeLayout.visibility = View.GONE
        } else {
            binding.previewTapLayout.visibility = View.GONE
            binding.previewSwipeLayout.visibility = View.VISIBLE
        }

        // 2. Update Tap Selection Card UI
        binding.choiceTap.apply {
            val isSelected = selectedMethod == "Tap"
            cardRoot.strokeColor = if (isSelected) pink else defaultStroke
            cardRoot.setCardBackgroundColor(if (isSelected) Color.parseColor("#24222B") else Color.parseColor("#1A1C23"))
            ivCheck.visibility = if (isSelected) View.VISIBLE else View.GONE
        }

        // 3. Update Swipe Selection Card UI
        binding.choiceSwipe.apply {
            val isSelected = selectedMethod == "Swipe"
            cardRoot.strokeColor = if (isSelected) pink else defaultStroke
            cardRoot.setCardBackgroundColor(if (isSelected) Color.parseColor("#24222B") else Color.parseColor("#1A1C23"))
            ivCheck.visibility = if (isSelected) View.VISIBLE else View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val width = if (resources.configuration.smallestScreenWidthDp >= 600) {
                (resources.displayMetrics.widthPixels * 0.90).toInt() // 60% width on Tablets
            } else {
                (resources.displayMetrics.widthPixels * 0.90).toInt() // 90% width on Phones
            }
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.CENTER)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}