package com.nzdeveloper.androidclock.alarmtimer.stopwatch.service

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.R
import android.content.ComponentName

class WidgetStopwatch : AppWidgetProvider() {

    companion object {
        const val ACTION_TOGGLE = "action_stopwatch_toggle"
        const val ACTION_RESET = "action_stopwatch_reset"
        private const val PREFS = "stopwatch_prefs"
        private const val KEY_BASE = "base_time"
        private const val KEY_IS_RUNNING = "is_running"
        private const val KEY_ELAPSED = "elapsed_time"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidgetUI(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        when (intent.action) {
            ACTION_TOGGLE -> {
                val isRunning = prefs.getBoolean(KEY_IS_RUNNING, false)
                if (isRunning) {
                    // PAUSE logic
                    val elapsed = SystemClock.elapsedRealtime() - prefs.getLong(KEY_BASE, SystemClock.elapsedRealtime())
                    prefs.edit().putBoolean(KEY_IS_RUNNING, false).putLong(KEY_ELAPSED, elapsed).apply()
                } else {
                    // START logic
                    val base = SystemClock.elapsedRealtime() - prefs.getLong(KEY_ELAPSED, 0L)
                    prefs.edit().putBoolean(KEY_IS_RUNNING, true).putLong(KEY_BASE, base).apply()
                }
            }
            ACTION_RESET -> {
                prefs.edit().putBoolean(KEY_IS_RUNNING, false).putLong(KEY_ELAPSED, 0L).putLong(KEY_BASE, SystemClock.elapsedRealtime()).apply()
            }
        }

        // Refresh all widgets after action
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, WidgetStopwatch::class.java))
        onUpdate(context, manager, ids)
    }

    private fun updateWidgetUI(context: Context, manager: AppWidgetManager, id: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_stopwatch)
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val isRunning = prefs.getBoolean(KEY_IS_RUNNING, false)

        // 1. Set Chronometer Time
        if (isRunning) {
            val base = prefs.getLong(KEY_BASE, SystemClock.elapsedRealtime())
            views.setChronometer(R.id.widget_chronometer, base, null, true)
            views.setImageViewResource(R.id.btn_widget_toggle, R.drawable.is_play)
        } else {
            val elapsed = prefs.getLong(KEY_ELAPSED, 0L)
            views.setChronometer(R.id.widget_chronometer, SystemClock.elapsedRealtime() - elapsed, null, false)
            views.setImageViewResource(R.id.btn_widget_toggle, R.drawable.is_pause)
        }

        // 2. Click Intents
        views.setOnClickPendingIntent(R.id.btn_widget_toggle, getPendingIntent(context, ACTION_TOGGLE))
        views.setOnClickPendingIntent(R.id.btn_widget_reset, getPendingIntent(context, ACTION_RESET))

        manager.updateAppWidget(id, views)
    }

    private fun getPendingIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, WidgetStopwatch::class.java).apply { this.action = action }
        return PendingIntent.getBroadcast(context, action.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}