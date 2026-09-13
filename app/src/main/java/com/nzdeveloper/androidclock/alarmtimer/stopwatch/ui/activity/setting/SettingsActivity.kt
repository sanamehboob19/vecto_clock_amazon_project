package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.activity.setting

import android.os.Bundle
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.R
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.ActivitySettingsBinding
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.base.BaseActivity
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.dilaog.DismissMethodDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

import android.content.Intent
import android.net.Uri
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.BuildConfig
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.activity.widget.WidgetHubActivity
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.dilaog.AboutDialog
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.TinyDB
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.ToastUtils

@AndroidEntryPoint
class SettingsActivity : BaseActivity<ActivitySettingsBinding>(ActivitySettingsBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()
        initClickListeners()
        setupDpadFocus()
    }

    /**
     * Makes all settings rows focusable and navigable via D-pad remote (Fire TV).
     * Without this, the settings screen is completely inaccessible on Fire TV.
     */
    private fun setupDpadFocus() {
        val allRows = listOf(
            binding.rowDismissMethod.rootSetting,
            binding.rowSilenceAfter.rootSetting,
            binding.rowSnoozeLength.rootSetting,
            binding.rowPrivacy.rootSetting,
            binding.rowRate.rootSetting,
            binding.rowFeedback.rootSetting,
            binding.rowShare.rootSetting,
            binding.rowAbout.rootSetting,
            binding.rowWidgetGuide.rootSetting,
            binding.rowSystemTime.rootSetting
        )

        allRows.forEach { row ->
            row.isFocusable = true
            row.isFocusableInTouchMode = false
            row.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    view.animate().scaleX(1.03f).scaleY(1.03f).setDuration(120).start()
                } else {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                }
            }
        }

        // Initial focus on back button when entering settings on TV
        binding.btnBack.post { binding.btnBack.requestFocus() }
    }


    private fun setupUI() {
        // --- SECTION: ALARMS ---
        binding.rowDismissMethod.apply {
            tvSettingTitle.text = "Dismiss method"
            tvSettingDesc.text = "Choose Swipe or Tap to stop ringing"
            tvSettingValue.text = tinyDB.getString("dismiss_method", "")
        }

        binding.rowSilenceAfter.apply {
            tvSettingTitle.text = "Silence after"
            tvSettingDesc.text = "Alarm turns off automatically after set time"
            tvSettingValue.text = tinyDB.getString("silence_after", "1 minute")
        }

        binding.rowSnoozeLength.apply {
            tvSettingTitle.text = "Snooze length"
            tvSettingDesc.text = "Minutes between snooze repeats"
            tvSettingValue.text = tinyDB.getString("snooze_length", "1 minute")
        }

        // --- SECTION: SUPPORT & INFO ---
        binding.rowPrivacy.apply {
            tvSettingTitle.text = "Privacy Policy"
            tvSettingDesc.text = "View how we handle and protect your data"
            tvSettingValue.text = "View"
        }

        binding.rowRate.apply {
            tvSettingTitle.text = "Rate Us"
            tvSettingDesc.text = "Support us by leaving a review on Play Store"
            tvSettingValue.text = ""
        }

        binding.rowFeedback.apply {
            tvSettingTitle.text = "Send Feedback"
            tvSettingDesc.text = "Report a bug or suggest a new feature"
            tvSettingValue.text = ""
        }

        binding.rowShare.apply {
            tvSettingTitle.text = "Share App"
            tvSettingDesc.text = "Invite your friends to use Vecto Clock"
            tvSettingValue.text = ""
        }

        binding.rowAbout.apply {
            tvSettingTitle.text = "About App"
            tvSettingDesc.text = "Version, legal info, and developer details"
            tvSettingValue.text = "v1.0.1"
        }

        binding.rowWidgetGuide.apply {
            tvSettingTitle.text = "Widget Gallery" // Changed from 'Widget Guide'
            tvSettingDesc.text = "Preview and add elegant clocks to your home screen"
            tvSettingValue.text = "Open"
        }

        binding.rowSystemTime.apply {
            tvSettingTitle.text = "Date & Time"
            tvSettingDesc.text = "Change system date, time, and timezone"
            tvSettingValue.text = "Edit"
        }



    }

    private fun initClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        // Alarm Settings
        binding.rowDismissMethod.rootSetting.setOnClickListener { showDismissDialog() }

        binding.rowSilenceAfter.rootSetting.setOnClickListener {
            val options = arrayOf("1 minute", "5 minutes", "10 minutes", "Never")
            showChoiceDialog("Silence after", options, "silence_after") {
                binding.rowSilenceAfter.tvSettingValue.text = it
            }
        }

        binding.rowSnoozeLength.rootSetting.setOnClickListener {
            val options = (1..30).map { if (it == 1) "$it minute" else "$it minutes" }.toTypedArray()
            showChoiceDialog("Snooze length", options, "snooze_length") {
                binding.rowSnoozeLength.tvSettingValue.text = it
            }
        }

        // Support & Social Actions
        binding.rowPrivacy.rootSetting.setOnClickListener { openUrl("https://sites.google.com/view/vectoclock/home") }

        binding.rowRate.rootSetting.setOnClickListener { openPlayStore() }

        binding.rowFeedback.rootSetting.setOnClickListener { sendEmailFeedback() }

        binding.rowShare.rootSetting.setOnClickListener { shareApp() }

//        binding.rowAbout.rootSetting.setOnClickListener { showAboutDialog() }

        binding.rowAbout.rootSetting.setOnClickListener {
            val dialog = AboutDialog()
            dialog.show(supportFragmentManager, "AboutDialog")
        }


        binding.rowWidgetGuide.rootSetting.setOnClickListener {
            val intent = Intent(this, WidgetHubActivity::class.java)
            startActivity(intent)

            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        binding.rowSystemTime.rootSetting.setOnClickListener {
            try {
                val intent = Intent(android.provider.Settings.ACTION_DATE_SETTINGS)
                startActivity(intent)
            } catch (e: Exception) {
                ToastUtils.show("Could not open system settings")
            }
        }


    }

    // --- LOGIC FUNCTIONS ---

    private fun showChoiceDialog(title: String, options: Array<String>, key: String, onSelected: (String) -> Unit) {
        val current = tinyDB.getString(key, "1 minute")
        var currentIndex = options.indexOf(current).coerceAtLeast(0)

        MaterialAlertDialogBuilder(this, R.style.CustomAlertDialogTheme)
            .setTitle(title)
            .setSingleChoiceItems(options, currentIndex) { dialog, which ->
                val selected = options[which]
                tinyDB.putString(key, selected)
                onSelected(selected)
                dialog.dismiss()
            }
            .show()
    }

    private fun showDismissDialog() {
        val dialog = DismissMethodDialog { selectedMethod ->
            binding.rowDismissMethod.tvSettingValue.text = selectedMethod
        }
        dialog.show(supportFragmentManager, "DismissMethodDialog")
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            ToastUtils.show("Could not open browser")
        }
    }

    private fun openPlayStore() {
        val appPackageName = packageName
        // Amazon Appstore URL (correct store for Amazon Fire TV / Tablet users)
        val amazonUrl = "amzn://apps/android?p=$appPackageName"
        val amazonWebUrl = "https://www.amazon.com/gp/mas/dl/android?p=$appPackageName"
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(amazonUrl)))
        } catch (e: Exception) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(amazonWebUrl)))
            } catch (e2: Exception) {
                ToastUtils.show("Could not open Amazon Appstore")
            }
        }
    }

    private fun sendEmailFeedback() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:nextchoiceonline@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, "Vecto Clock Feedback")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            ToastUtils.show("No email app found")
        }
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            val shareMessage = "Check out Smart Clock — the most elegant clock app!\n\nhttps://www.amazon.com/gp/mas/dl/android?p=$packageName"
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(this, R.style.CustomAlertDialogTheme)
            .setTitle("Smart Clock")
            .setMessage("Developed by NZ Developer\nVersion ${BuildConfig.VERSION_NAME}\n\n\u00a9 2024 All Rights Reserved.")
            .setPositiveButton("Close", null)
            .show()
    }
}