package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.activity.main

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.R
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.ActivityMainBinding
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.activity.bedTime.BedtimeActivity
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.activity.setting.SettingsActivity
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.base.BaseActivity
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.dilaog.ExitDialog
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.fragments.AlarmFragment
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.fragments.ClockFragment
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.fragments.MoreFragment
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.fragments.StopwatchFragment
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.fragments.TimerFragment
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.vm.MainViewModel
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.DeviceUtils
import dagger.hilt.android.AndroidEntryPoint



@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private val viewModel: MainViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isTV = DeviceUtils.isFireTV(this)
        if (isTV) {
            binding.btnAlarm.visibility = android.view.View.GONE
        }

        initViewPager(isTV)
        initBottomNavigation(isTV)
        selectTab(viewModel.currentTabIndex, isTV)

        // Settings Button Animation & Navigation
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnSettings.setOnFocusChangeListener { _, hasFocus ->
            val pink = Color.parseColor("#FFB6C1")
            val white = Color.WHITE
            if (hasFocus) {
                binding.btnSettings.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150).start()
                binding.imgSettings.setColorFilter(pink)
            } else {
                binding.btnSettings.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start()
                binding.imgSettings.setColorFilter(white)
            }
        }

        if (DeviceUtils.needsDpadNavigation(this)) {
            setupDpadFocusLogic(isTV)

            // Push focus into fragment content on cold launch instead of
            // letting Android auto-focus the first view (Settings) in the XML.
            binding.root.post {
                focusActiveFragmentContent()
            }
        }

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.currentTabIndex != 0) {
                    selectTab(0, isTV)
                } else {
                    showExitDialog()
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupDpadFocusLogic(isTV: Boolean) {
        // Ensure ViewPager allows its children to be focused
        val innerPager = binding.viewPager.getChildAt(0) as? androidx.recyclerview.widget.RecyclerView
        innerPager?.focusable = android.view.View.FOCUSABLE
        binding.viewPager.descendantFocusability = android.view.ViewGroup.FOCUS_BEFORE_DESCENDANTS

        val navButtons = listOf(binding.btnSettings, binding.btnClock, binding.btnAlarm, binding.btnTimer, binding.btnStopwatch, binding.btnMore)

        // NOTE: No setOnKeyListener here anymore.
        // We rely ONLY on XML nextFocusRight/nextFocusLeft attributes for
        // in-tab D-pad movement — having both a manual listener AND the XML
        // attribute fire on the same key press was causing the focus
        // flicker (boundary appears then disappears).
        navButtons.forEach { btn ->
            btn.isFocusable = true
            btn.isFocusableInTouchMode = true
        }

        // Standard up/down navigation for Sidebar
        binding.btnSettings.nextFocusDownId = binding.btnClock.id
        binding.btnClock.nextFocusUpId = binding.btnSettings.id
        binding.btnClock.nextFocusDownId = if (isTV) binding.btnTimer.id else binding.btnAlarm.id

        if (!isTV) {
            binding.btnAlarm.nextFocusUpId = binding.btnClock.id
            binding.btnAlarm.nextFocusDownId = binding.btnTimer.id
        }

        binding.btnTimer.nextFocusUpId = if (isTV) binding.btnClock.id else binding.btnAlarm.id
        binding.btnTimer.nextFocusDownId = binding.btnStopwatch.id

        binding.btnStopwatch.nextFocusUpId = binding.btnTimer.id
        binding.btnStopwatch.nextFocusDownId = binding.btnMore.id

        binding.btnMore.nextFocusUpId = binding.btnStopwatch.id
    }

    /**
     * Moves D-pad focus into the currently visible fragment's primary control.
     * Retries a few times with a short delay because right after a
     * ViewPager2 tab switch, the fragment's view / RecyclerView ViewHolder
     * may not be attached/laid out yet on the first attempt.
     */
    fun focusActiveFragmentContent(retries: Int = 6) {
        val pageIndex = binding.viewPager.currentItem
        val navHostView = binding.viewPager.getChildAt(0) as? androidx.recyclerview.widget.RecyclerView
        val viewHolder = navHostView?.findViewHolderForAdapterPosition(pageIndex)
        val itemView = viewHolder?.itemView

        if (itemView == null) {
            if (retries > 0) {
                binding.viewPager.postDelayed({ focusActiveFragmentContent(retries - 1) }, 80)
            }
            return
        }

        val target: android.view.View? = when (pageIndex) {
            0 -> itemView.findViewById(R.id.btn_digital) ?: itemView.findViewById(R.id.btn_classic)
            1 -> itemView.findViewById(R.id.btn_play_pause) // Timer
            2 -> itemView.findViewById(R.id.btn_start_stop) // Stopwatch
            3 -> itemView.findViewById(R.id.card_bedtime_main)
            else -> null
        }

        if (target == null) {
            if (retries > 0) {
                binding.viewPager.postDelayed({ focusActiveFragmentContent(retries - 1) }, 80)
            }
            return
        }

        target.apply {
            isFocusable = true
            isFocusableInTouchMode = true
            postDelayed({
                requestFocus()
                if (!hasFocus()) requestFocusFromTouch()
            }, 30)
        }
    }

    private fun selectTab(index: Int, isTV: Boolean) {
        val maxIndex = if (isTV) 3 else 4
        val safeIndex = index.coerceIn(0, maxIndex)
        viewModel.currentTabIndex = safeIndex
        binding.viewPager.setCurrentItem(safeIndex, false)
        updateUI(safeIndex, isTV)

        // Delay slightly to allow ViewPager to settle before moving D-pad focus
        if (DeviceUtils.needsDpadNavigation(this)) {
            binding.viewPager.postDelayed({ focusActiveFragmentContent() }, 150)
        }
    }

    private fun initViewPager(isTV: Boolean) {
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = if (isTV) 4 else 5
            override fun createFragment(position: Int): Fragment {
                return if (isTV) {
                    when (position) {
                        0 -> ClockFragment()
                        1 -> TimerFragment()
                        2 -> StopwatchFragment()
                        3 -> MoreFragment()
                        else -> ClockFragment()
                    }
                } else {
                    when (position) {
                        0 -> ClockFragment()
                        1 -> AlarmFragment()
                        2 -> TimerFragment()
                        3 -> StopwatchFragment()
                        4 -> MoreFragment()
                        else -> ClockFragment()
                    }
                }
            }
        }
        binding.viewPager.isUserInputEnabled = false
    }

    private fun initBottomNavigation(isTV: Boolean) {
        binding.btnClock.setOnClickListener { selectTab(0, isTV) }
        if (!isTV) binding.btnAlarm.setOnClickListener { selectTab(1, isTV) }
        binding.btnTimer.setOnClickListener { selectTab(if (isTV) 1 else 2, isTV) }
        binding.btnStopwatch.setOnClickListener { selectTab(if (isTV) 2 else 3, isTV) }
        binding.btnMore.setOnClickListener { selectTab(if (isTV) 3 else 4, isTV) }
    }

    private fun updateUI(index: Int, isTV: Boolean) {
        val activeColor = Color.parseColor("#FFB6C1")
        val inactiveColor = Color.parseColor("#8E8E8E")

        binding.txtLabel.text = if (isTV) {
            when (index) { 0 -> "Clock"; 1 -> "Timer"; 2 -> "Stopwatch"; 3 -> "More"; else -> "Clock" }
        } else {
            when (index) { 0 -> "Clock"; 1 -> "Alarm"; 2 -> "Timer"; 3 -> "Stopwatch"; 4 -> "More"; else -> "Clock" }
        }

        val buttons = if (isTV) listOf(binding.btnClock, binding.btnTimer, binding.btnStopwatch, binding.btnMore)
        else listOf(binding.btnClock, binding.btnAlarm, binding.btnTimer, binding.btnStopwatch, binding.btnMore)

        val texts = if (isTV) listOf(binding.tvClock, binding.tvTimer, binding.tvStopwatch, binding.tvMore)
        else listOf(binding.tvClock, binding.tvAlarm, binding.tvTimer, binding.tvStopwatch, binding.tvMore)

        val icons = if (isTV) listOf(binding.imgClock, binding.imgTimer, binding.imgStopwatch, binding.imgMore)
        else listOf(binding.imgClock, binding.imgAlarm, binding.imgTimer, binding.imgStopwatch, binding.imgMore)

        for (i in buttons.indices) {
            val color = if (i == index) activeColor else inactiveColor
            texts[i].setTextColor(color)
            icons[i].setColorFilter(color)
            if (i == index) buttons[i].animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).start()
            else buttons[i].animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
        }
    }

    private fun showExitDialog() {
        ExitDialog().show(supportFragmentManager, "ExitDialog")
    }
}








