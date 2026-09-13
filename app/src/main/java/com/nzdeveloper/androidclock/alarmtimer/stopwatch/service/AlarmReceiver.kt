package com.nzdeveloper.androidclock.alarmtimer.stopwatch.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.R
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.db.AppDatabase
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.Alarm
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.SleepSchedule
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.activity.bedTime.BedtimeActivity
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.activity.sunrise.SunriseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        val action = intent.action
        val actionType = intent.getStringExtra("ACTION_TYPE")
        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        val label = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("ALARM_DEBUG", "Received Alarm ID: $alarmId with actionType: $actionType")
                val db = AppDatabase.getInstance(context)
                val alarmDao = db.alarmDao()
                val sleepDao = db.sleepDao()

                when {
                    // 1. SUNRISE: Early brightening logic
                    actionType == "SUNRISE" -> {
                        val i = Intent(context, SunriseActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        context.startActivity(i)
                    }

                    // 2. BEDTIME START: Persistent Notification and UI Sync
                    actionType == "START_BEDTIME_MODE" -> {
                        val schedule = sleepDao.getScheduleSync()

                        // Show "Bedtime mode is on... until [Wakeup Time]"
                        showBedtimeModeNotification(context, schedule)

                        // Open Activity with Grayscale flag for App-Level UI
                        val i = Intent(context, BedtimeActivity::class.java).apply {
                            putExtra("OPEN_SLEEP_OVERLAY", true)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        context.startActivity(i)

                        // Schedule the next cycle immediately
                        rescheduleSleepFlow(context)
                    }

                    // 3. BEDTIME REMINDER: 15-min warning
                    actionType == "BEDTIME_REMINDER" -> {
                        showBedtimeReminderNotification(context)
                    }

                    // 4. ACTION SNOOZE: Temporary Alarm
                    action == AlarmService.ACTION_SNOOZE -> {
                        context.stopService(Intent(context, AlarmService::class.java))

                        val sharedPref = context.getSharedPreferences("vecto_clock", Context.MODE_PRIVATE)
                        val snoozeSetting = sharedPref.getString("snooze_length", "1 minute") ?: "1 minute"
                        val minutes = snoozeSetting.split(" ")[0].toIntOrNull() ?: 1

                        val calendar = Calendar.getInstance().apply { add(Calendar.MINUTE, minutes) }

                        val snoozeAlarm = Alarm(
                            id = (System.currentTimeMillis() % 100000).toInt(),
                            hour = calendar.get(Calendar.HOUR_OF_DAY),
                            minute = calendar.get(Calendar.MINUTE),
                            amPm = if (calendar.get(Calendar.HOUR_OF_DAY) >= 12) "PM" else "AM",
                            label = "Snooze: $label",
                            isEnabled = true
                        )
                        AlarmScheduler.schedule(context, snoozeAlarm)

                        val nextTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)
                        showSummaryNotification(context, "Snoozed", "Next ring at $nextTime")
                    }

                    // 5. ACTION STOP: User dismissed the ringing
                    action == AlarmService.ACTION_STOP || action == "STOP_FROM_SWIPE" -> {
                        context.stopService(Intent(context, AlarmService::class.java))

                        //  Trigger "Good Morning" only for Wakeup Alarm (9998)
                        if (alarmId == 9998) {
                            showWakeUpSummary(context)
                        } else {
                            val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                            showSummaryNotification(context, "Alarm Stopped", "Dismissed at $currentTime")
                        }

                        // DB Update for Once-off alarms
                        val alarm = alarmDao.getAlarmByIdSync(alarmId)
                        if (alarm != null && alarm.days.isEmpty()) {
                            alarmDao.updateAlarm(alarm.copy(isEnabled = false))

                            // Sync Master Switch if it's the Wakeup Alarm
                            if (alarmId == 9998) {
                                val schedule = sleepDao.getScheduleSync()
                                schedule?.let { sleepDao.saveSchedule(it.copy(isEnabled = false)) }
                            }
                        }
                    }

                    // 6. DEFAULT: START ALARM RINGING
                    else -> {
                        if (alarmId != -1) {
                            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                                putExtra("ALARM_ID", alarmId)
                                putExtra("ALARM_LABEL", label)
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(serviceIntent)
                            } else {
                                context.startService(serviceIntent)
                            }

                            //  RESCHEDULING LOGIC
                            CoroutineScope(Dispatchers.IO).launch {
                                val db = AppDatabase.getInstance(context)

                                if (alarmId == 9998) {
                                    val schedule = db.sleepDao().getScheduleSync()
                                    if (schedule != null && schedule.isEnabled) {
                                        withContext(Dispatchers.Main) {
                                            Log.d("ALARM_DEBUG", "Rescheduling Wakeup Alarm (9998) for tomorrow")
                                            AlarmScheduler.scheduleSleepFlow(context, schedule)
                                        }
                                    }
                                } else {
                                    val alarm = db.alarmDao().getAlarmByIdSync(alarmId)
                                    if (alarm != null) {
                                        if (alarm.days.isEmpty()) {
                                            db.alarmDao().updateAlarm(alarm.copy(isEnabled = false))
                                        } else {
                                            withContext(Dispatchers.Main) {
                                                Log.d("ALARM_DEBUG", "Rescheduling Normal Alarm (${alarm.id}) for next day")
                                                AlarmScheduler.schedule(context, alarm)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }


                }
            } catch (e: Exception) {
                Log.e("ALARM_DEBUG", "Error in Receiver: ${e.message}")
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    // --- NOTIFICATION HELPERS ---

    private fun showBedtimeModeNotification(context: Context, schedule: SleepSchedule?) {
        val channelId = "bedtime_mode_status_channel"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Bedtime Mode Status", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        val wakeupTime = if (schedule != null) {
            val h = schedule.wakeUpHour
            val dh = when { h == 0 -> 12; h > 12 -> h - 12; else -> h }
            val amPm = if (h >= 12) "pm" else "am"
            String.format("%d:%02d %s", dh, schedule.wakeUpMinute, amPm)
        } else "morning"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_bedtime_moon)
            .setContentTitle("Bedtime mode is on")
            .setContentText("Screen stays dark until $wakeupTime")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()
        manager.notify(4004, notification)
    }

    private fun showWakeUpSummary(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "wakeup_summary_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Good Morning", NotificationManager.IMPORTANCE_HIGH)
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
        manager.cancel(4004) // Dismiss Bedtime notification
    }

    private fun showBedtimeReminderNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "bedtime_reminder_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Bedtime Reminder", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_bedtime_moon)
            .setContentTitle("Bedtime Reminder")
            .setContentText("It's almost bedtime. Time to wind down!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify(2002, notification)
    }

    private fun showSummaryNotification(context: Context, title: String, message: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "general_history_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(NotificationChannel(channelId, "Alarm Summary", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .build()
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun rescheduleSleepFlow(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(context)
            val schedule = db.sleepDao().getScheduleSync()
            schedule?.let {
                AlarmScheduler.scheduleSleepFlow(context, it)
            }
        }
    }
}



