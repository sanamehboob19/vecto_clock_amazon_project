package com.nzdeveloper.androidclock.alarmtimer.stopwatch.service


import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.R
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.activity.alarmRinging.RingingActivity
import android.widget.RemoteViews
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AlarmService : Service() {

    companion object {
        private const val CHANNEL_ID = "ALARM_RINGING_CHANNEL_V2"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_SNOOZE = "ACTION_SNOOZE"
    }

    private var vibrator: Vibrator? = null
    private val handler = Handler(Looper.getMainLooper())
    private var silenceRunnable: Runnable? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val alarmId = intent?.getIntExtra("ALARM_ID", -1) ?: -1
        val label = intent?.getStringExtra("ALARM_LABEL") ?: "Alarm"

        // 1. Setup Custom Notification View
        val customView = RemoteViews(packageName, R.layout.layout_notification_alarm)
        customView.setTextViewText(R.id.tv_noti_title, label)
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        customView.setTextViewText(R.id.tv_noti_time_info, "${sdf.format(Date())} • Swipe to stop")

        // 2. Setup Intents (Stop, Snooze, Delete)
        val stopIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = ACTION_STOP
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", label)
        }

        val pStop = PendingIntent.getBroadcast(this, alarmId + 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val snoozeIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", label)
        }
        val pSnooze = PendingIntent.getBroadcast(this, alarmId + 2, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val pDelete = PendingIntent.getBroadcast(this, alarmId + 3, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        customView.setOnClickPendingIntent(R.id.btn_noti_stop, pStop)
        customView.setOnClickPendingIntent(R.id.btn_noti_snooze, pSnooze)

        // 3. Full Screen Intent
        val fullScreenIntent = Intent(this, RingingActivity::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", label)
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(this, alarmId, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // 4. Build Notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setCustomContentView(customView)
            .setCustomHeadsUpContentView(customView)
            .setDeleteIntent(pDelete)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        // 5. Check Database for Vibration Setting
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(applicationContext)
            val alarm = db.alarmDao().getAlarmByIdSync(alarmId)

            // Only vibrate if the database record actually has isVibrate = true
            if (alarmId == 9998 || alarm?.isVibrate == true || alarm == null) {
                withContext(Dispatchers.Main) {
                    startContinuousVibration()
                }
            }
        }

        // 6. Auto-Silence Logic
        val sharedPref = getSharedPreferences("vecto_clock", Context.MODE_PRIVATE)
        val silenceSetting = sharedPref.getString("silence_after", "10 minutes") ?: "10 minutes"

        if (silenceSetting != "Never") {
            val minutes = silenceSetting.split(" ")[0].toLongOrNull() ?: 10L
            silenceRunnable = Runnable {
                val timeoutIntent = Intent(this, AlarmReceiver::class.java).apply {
                    action = ACTION_STOP
                    putExtra("ALARM_ID", alarmId)
                    putExtra("ALARM_LABEL", label)
                    putExtra("FROM_TIMEOUT", true)
                }
                sendBroadcast(timeoutIntent)
            }
            handler.postDelayed(silenceRunnable!!, minutes * 60 * 1000)
        }

        // 7. Start Foreground
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) { e.printStackTrace() }

        return START_STICKY
    }

    private fun startContinuousVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION") getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        val pattern = longArrayOf(0, 1000, 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION") vibrator?.vibrate(pattern, 0)
        }
    }

    override fun onDestroy() {
        vibrator?.cancel()
        silenceRunnable?.let { handler.removeCallbacks(it) }
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM).build()
            val channel = NotificationChannel(CHANNEL_ID, "Alarm Active", NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(null, audioAttributes)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }
    }
}