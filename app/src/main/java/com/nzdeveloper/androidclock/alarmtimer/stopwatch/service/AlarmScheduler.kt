package com.nzdeveloper.androidclock.alarmtimer.stopwatch.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.Alarm
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.SleepSchedule
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


object AlarmScheduler {

    // Unique IDs for Sleep features
    private const val ID_WAKE_UP_ALARM = 9998
    private const val ID_BEDTIME_REMINDER = 9999
    private const val ID_SUNRISE_TRIGGER = 9997
    private const val ID_BEDTIME_MODE_START = 9996

    fun schedule(context: Context, alarm: Alarm) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
            putExtra("ALARM_LABEL", alarm.label)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis() + 1000) {
            if (alarm.days.isEmpty()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val now = System.currentTimeMillis()

        if (alarm.days.isNotEmpty()) {
            // REPEATING ALARM LOGIC
            val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
            var daysUntilNext = -1

            for (i in 0..7) {
                val checkDay = (today + i - 1) % 7 + 1
                if (alarm.days.contains(checkDay)) {
                    // If checkDay is today, check if time has already passed
                    if (i == 0 && calendar.timeInMillis <= now) continue

                    daysUntilNext = i
                    break
                }
            }
            if (daysUntilNext != -1) calendar.add(Calendar.DAY_OF_MONTH, daysUntilNext)
        } else {
            // ONE-TIME ALARM LOGIC
            if (calendar.timeInMillis <= now) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val sdf = SimpleDateFormat("EEEE, dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        Log.d("ALARM_DEBUG", "Scheduling Alarm ID: ${alarm.id} for ${sdf.format(calendar.time)}")


        // Schedule Precisely
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } else {
                Log.e("ALARM_DEBUG", "Exact Alarm Permission NOT GRANTED. Falling back to inexact.")
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    fun cancelById(context: Context, alarmId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarmId, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    /**
     * 4. BEDTIME FLOW SCHEDULER
     * Handles the complex Bedtime reminder and Wake-up alarm logic.
     */
    /**
     * Professional Sleep Flow Scheduler
     * Handles: Wakeup Alarm, Sunrise, Bedtime Start, and Bedtime Reminder
     */
    fun scheduleSleepFlow(context: Context, schedule: SleepSchedule) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 1. IF DISABLED: Cancel everything and stop
        if (!schedule.isEnabled) {
            cancelById(context, ID_WAKE_UP_ALARM)
            cancelById(context, ID_BEDTIME_REMINDER)
            cancelById(context, ID_SUNRISE_TRIGGER)
            cancelById(context, ID_BEDTIME_MODE_START)
            return
        }

        // -------------------------------------------------------
        // A. WAKE-UP ALARM (The actual Ringing)
        // -------------------------------------------------------
        val wakeupAlarm = Alarm(
            id = ID_WAKE_UP_ALARM,
            hour = schedule.wakeUpHour,
            minute = schedule.wakeUpMinute,
            days = schedule.days,
            label = "Wake up",
            amPm = if (schedule.wakeUpHour >= 12) "PM" else "AM",
            isEnabled = true,
            isVibrate = true
        )
        schedule(context, wakeupAlarm)

        // -------------------------------------------------------
        // B. SUNRISE TRIGGER (15 mins before Wake-up)
        // -------------------------------------------------------
        if (schedule.isSunriseAlarm) {
            val sunriseCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, schedule.wakeUpHour)
                set(Calendar.MINUTE, schedule.wakeUpMinute)
                set(Calendar.SECOND, 0)
                add(Calendar.MINUTE, -15)

                if (before(Calendar.getInstance())) {
                    add(Calendar.DATE, 1)
                }
            }
            createSleepBroadcast(context, ID_SUNRISE_TRIGGER, "SUNRISE", sunriseCal.timeInMillis)
        }

        // -------------------------------------------------------
        // C. BEDTIME MODE START (The Grey Scale / Detox start)
        // -------------------------------------------------------
        val bedtimeCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, schedule.bedtimeHour)
            set(Calendar.MINUTE, schedule.bedtimeMinute)
            set(Calendar.SECOND, 0)

            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }
        createSleepBroadcast(context, ID_BEDTIME_MODE_START, "START_BEDTIME_MODE", bedtimeCal.timeInMillis)

        // -------------------------------------------------------
        // D. SMART BEDTIME REMINDER (15 mins before Bedtime)
        // -------------------------------------------------------
        if (schedule.isBedtimeReminder) {
            val now = System.currentTimeMillis()
            val fifteenMinsBeforeBedtime = bedtimeCal.timeInMillis - (15 * 60 * 1000)

            if (fifteenMinsBeforeBedtime > now) {
                createSleepBroadcast(context, ID_BEDTIME_REMINDER, "BEDTIME_REMINDER", fifteenMinsBeforeBedtime)
            } else if (bedtimeCal.timeInMillis > now) {
                createSleepBroadcast(context, ID_BEDTIME_REMINDER, "BEDTIME_REMINDER", now + 5000)
            }
        }
    }

    /**
     * Helper to create Broadcasts for Sleep/Bedtime triggers
     */
    private fun createSleepBroadcast(context: Context, id: Int, actionType: String, timeMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ACTION_TYPE", actionType)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
        }
    }


}