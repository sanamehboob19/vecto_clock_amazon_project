package com.nzdeveloper.androidclock.alarmtimer.stopwatch.service

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.R
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


import android.view.View
import kotlin.math.abs

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.activity.worldClock.WorldClockActivity


class WidgetWorldClock : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val db = AppDatabase.getInstance(context)

        CoroutineScope(Dispatchers.IO).launch {
            // Fetch first 4 cities from Database
            val cities = db.worldClockDao().getAllCitiesSync()
            val count = cities.size

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_world_clock_grid)

                // 1. GLOBAL CLICK: Open World Clock Activity in App
                val intent = Intent(context, WorldClockActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context, 10, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_world_root, pendingIntent)

                // 2. EMPTY STATE LOGIC
                if (cities.isEmpty()) {
                    views.setViewVisibility(R.id.layout_empty_widget, View.VISIBLE)
                    views.setViewVisibility(R.id.layout_cities_grid, View.GONE)
                } else {
                    views.setViewVisibility(R.id.layout_empty_widget, View.GONE)
                    views.setViewVisibility(R.id.layout_cities_grid, View.VISIBLE)

                    // 3. DIVIDER LOGIC (Professional UI)
                    // Vertical line in Row 1 only if 2+ cities exist
                    views.setViewVisibility(R.id.v_div1, if (count >= 2) View.VISIBLE else View.GONE)
                    // Horizontal line between rows only if 3+ cities exist
                    views.setViewVisibility(R.id.h_div, if (count >= 3) View.VISIBLE else View.GONE)
                    // Vertical line in Row 2 only if 4 cities exist
                    views.setViewVisibility(R.id.v_div2, if (count == 4) View.VISIBLE else View.GONE)

                    // 4. ADAPTIVE ROW LOGIC
                    // Show Row 2 only if user has added 3 or more cities
                    views.setViewVisibility(R.id.row2, if (count >= 3) View.VISIBLE else View.GONE)

                    // Mapping lists for easy looping
                    val slotIds = listOf(R.id.slot1, R.id.slot2, R.id.slot3, R.id.slot4)
                    val nameIds = listOf(R.id.name1, R.id.name2, R.id.name3, R.id.name4)
                    val timeIds = listOf(R.id.time1, R.id.time2, R.id.time3, R.id.time4)
                    val diffIds = listOf(R.id.diff1, R.id.diff2, R.id.diff3, R.id.diff4)

                    for (i in 0 until 4) {
                        if (i < count) {
                            val city = cities[i]

                            // Show Slot
                            views.setViewVisibility(slotIds[i], View.VISIBLE)

                            // Clean City Name (LONDON, NEW YORK, etc.)
                            val cleanName = city.cityName.split(" (")[0].uppercase()
                            views.setTextViewText(nameIds[i], cleanName)

                            // Link TextClock to the correct TimeZone
                            views.setString(timeIds[i], "setTimeZone", city.timeZoneId)

                            // Set GMT Difference (+5H, -3H, etc.)
                            views.setTextViewText(diffIds[i], getTimeDiff(city.timeZoneId))

                        } else {
                            // Hide empty slots so active ones fill the space (using weights)
                            views.setViewVisibility(slotIds[i], View.GONE)
                        }
                    }
                }

                // Push updates to the phone's Home Screen
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    /**
     * Professional Helper: Calculates real-time GMT offset for the widget label
     */
    private fun getTimeDiff(targetId: String): String {
        val targetTz = TimeZone.getTimeZone(targetId)
        val localTz = TimeZone.getDefault()

        val diffMillis = targetTz.getOffset(System.currentTimeMillis()) - localTz.getOffset(System.currentTimeMillis())
        val diffHours = diffMillis / (1000 * 60 * 60)

        return when {
            diffHours > 0 -> "+${diffHours}H"
            diffHours < 0 -> "-${abs(diffHours)}H"
            else -> "LOCAL"
        }
    }

    companion object {
        /**
         * Call this from WorldClockViewModel to force a widget UI refresh
         */
        fun refreshWidget(context: Context) {
            val intent = Intent(context, WidgetWorldClock::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

            val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, WidgetWorldClock::class.java))

            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}