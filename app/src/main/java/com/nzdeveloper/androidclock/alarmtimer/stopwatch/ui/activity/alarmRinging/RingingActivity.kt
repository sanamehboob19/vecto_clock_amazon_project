package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.activity.alarmRinging

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.*
import android.view.WindowManager
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.ActivityRingingBinding
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.Alarm
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.service.AlarmScheduler
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.service.AlarmService
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.ToastUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.net.Uri
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.db.AlarmDao
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.TinyDB
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.abs
import androidx.activity.OnBackPressedCallback
import androidx.core.app.NotificationCompat
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.R
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.db.AppDatabase
import kotlinx.coroutines.*

@AndroidEntryPoint
class RingingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRingingBinding

    @Inject lateinit var tinyDB: TinyDB
    @Inject lateinit var alarmDao: AlarmDao

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private val activityScope = CoroutineScope(Dispatchers.Main + Job())
    private val handler = Handler(Looper.getMainLooper())

    private var alarmId: Int = -1
    private var alarmLabel: String = "Alarm"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Wake up screen and show over lockscreen
        setupLockScreenFlags()

        binding = ActivityRingingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Get Data
        alarmId = intent.getIntExtra("ALARM_ID", -1)
        alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"

        // 3. Initialize UI
        setupStaticUI()
        setupInteractionMethod()

        // 4. Start Media (Sound & Vibration)
        startAlarmEffects()

        // 5. Setup Auto-Silence Timer
        setupAutoSilence()

        // Disable back button during ringing
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { /* Do nothing to prevent escaping alarm */ }
        })
    }

    private fun setupStaticUI() {
        binding.tvAlarmLabel.text = alarmLabel
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        binding.tvRingingTime.text = timeFormat.format(Date())
    }

    private fun setupInteractionMethod() {
        val method = tinyDB.getString("dismiss_method", "Swipe")
        if (method == "Tap") {
            binding.layoutTapUi.visibility = View.VISIBLE
            binding.layoutSwipeUi.visibility = View.GONE
            binding.btnTapSnooze.setOnClickListener { snoozeAlarm() }
            binding.btnTapStop.setOnClickListener { dismissAlarm() }
        } else {
            binding.layoutSwipeUi.visibility = View.VISIBLE
            binding.layoutTapUi.visibility = View.GONE
            setupSwipeLogic()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSwipeLogic() {
        var initialX = 0f
        val swipeThreshold = 200f

        binding.ivSwipeThumb.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = event.rawX
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialX
                    // Constrain movement within the track
                    if (abs(deltaX) < 450) {
                        view.translationX = deltaX
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val finalDeltaX = event.rawX - initialX
                    when {
                        finalDeltaX > swipeThreshold -> dismissAlarm() // Swiped Right
                        finalDeltaX < -swipeThreshold -> snoozeAlarm()  // Swiped Left
                        else -> view.animate().translationX(0f).setDuration(200).start() // Reset
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun startAlarmEffects() {
        activityScope.launch {
            // 1. Fetch alarm from DB
            val alarm = withContext(Dispatchers.IO) { alarmDao.getAlarmByIdSync(alarmId) }

            try {
                // 2. LOGIC FOR RINGTONE
                val ringtoneUri = if (alarm != null && !alarm.ringtoneUri.isNullOrEmpty()) {
                    Uri.parse(alarm.ringtoneUri)
                } else {
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                }

                ringtone = RingtoneManager.getRingtone(applicationContext, ringtoneUri)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ringtone?.audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                }
                ringtone?.play()

                // 3. LOGIC FOR VIBRATION
                // If alarm is NULL (Snooze/Temp Alarm) OR alarmId is Wakeup (9998)
                // OR DB says isVibrate is true -> Then Vibrate.
                val shouldVibrate = alarm == null || alarmId == 9998 || alarm.isVibrate

                if (shouldVibrate) {
                    vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                        vibratorManager.defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    }

                    val pattern = longArrayOf(0, 800, 800) // Pulse: 800ms on, 800ms off
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0)) // 0 means loop
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(pattern, 0)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Final fallback: If everything fails, try playing any system sound
                try {
                    val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    RingtoneManager.getRingtone(applicationContext, notification).play()
                } catch (e2: Exception) { }
            }
        }
    }

    private fun dismissAlarm() {
        Log.d("ALARM_DEBUG", "Dismissing Alarm. Current ID: $alarmId")

        // 2. STOP EVERYTHING IMMEDIATELY
        stopEverything()

        // 3. SHOW NOTIFICATION FIRST
        if (alarmId == 9998) {
            Log.d("ALARM_DEBUG", "Wakeup ID detected! Showing Good Morning Notification...")
            showWakeUpSummaryManual(applicationContext)
        }

        // 4. DATABASE UPDATE
        val context = applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)

                // Wakeup Schedule Sync
                if (alarmId == 9998) {
                    val schedule = db.sleepDao().getScheduleSync()
                    if (schedule != null && schedule.days.isEmpty()) {
                        db.sleepDao().saveSchedule(schedule.copy(isEnabled = false))
                    }
                }

                // Normal Alarm Sync
                val alarm = db.alarmDao().getAlarmByIdSync(alarmId)
                if (alarm != null && alarm.days.isEmpty()) {
                    db.alarmDao().updateAlarm(alarm.copy(isEnabled = false))
                }
            } catch (e: Exception) {
                Log.e("ALARM_DEBUG", "DB Update Error: ${e.message}")
            }
        }

        // 5. FINISH UI
        ToastUtils.show("Alarm Stopped")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask()
        } else {
            finish()
        }
    }


    private fun showWakeUpSummaryManual(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "wakeup_summary_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, "Good Morning", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_wakeup_sun)
            .setContentTitle("Good Morning!")
            .setContentText("Your sleep schedule is complete. Have a great day!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(5005, notification)
        manager.cancel(4004)
    }
    private fun snoozeAlarm() {
        stopEverything()

        activityScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(applicationContext)
            val alarmDao = db.alarmDao()

            val currentAlarm = alarmDao.getAlarmByIdSync(alarmId)

            val currentRingtoneUri = currentAlarm?.ringtoneUri ?: ""
            val currentVibrateSetting = currentAlarm?.isVibrate ?: true

            val snoozeSetting = tinyDB.getString("snooze_length", "10 minutes")
            val minutes = snoozeSetting.split(" ")[0].toIntOrNull() ?: 10
            val calendar = Calendar.getInstance().apply { add(Calendar.MINUTE, minutes) }

            val snoozeAlarm = Alarm(
                id = (System.currentTimeMillis() % 100000).toInt(),
                hour = calendar.get(Calendar.HOUR_OF_DAY),
                minute = calendar.get(Calendar.MINUTE),
                amPm = if (calendar.get(Calendar.HOUR_OF_DAY) >= 12) "PM" else "AM",
                label = "Snooze: $alarmLabel",
                isEnabled = true,
                ringtoneUri = currentRingtoneUri,
                isVibrate = currentVibrateSetting
            )

            withContext(Dispatchers.Main) {
                AlarmScheduler.schedule(this@RingingActivity, snoozeAlarm)
                ToastUtils.show("Snoozed for $minutes minutes")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAndRemoveTask()
                } else {
                    finish()
                }
            }
        }
    }

    private fun stopEverything() {
        try {
            ringtone?.stop()
            ringtone = null
            vibrator?.cancel()
            vibrator = null
            handler.removeCallbacksAndMessages(null)
            stopService(Intent(this, AlarmService::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupAutoSilence() {
        val silenceSetting = tinyDB.getString("silence_after", "10 minutes")
        if (silenceSetting != "Never") {
            val minutes = silenceSetting.split(" ")[0].toLongOrNull() ?: 10L
            handler.postDelayed({
                dismissAlarm()
                ToastUtils.show("Alarm silenced automatically")
            }, minutes * 60 * 1000)
        }
    }

    private fun setupLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    override fun onDestroy() {
        stopEverything()
        activityScope.cancel()
        super.onDestroy()
    }


}