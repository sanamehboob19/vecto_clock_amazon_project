package com.nzdeveloper.androidclock.alarmtimer.stopwatch.service

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.R
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.db.AppDatabase
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.model.City
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.activity.worldClock.WorldClockActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone






class WidgetDualStacked : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val db = AppDatabase.getInstance(context)

        CoroutineScope(Dispatchers.IO).launch {
            val cities = db.worldClockDao().getAllCitiesSync()
            val count = cities.size

            for (id in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_dual_stacked)

                val intent = Intent(context, WorldClockActivity::class.java)
                val pi = PendingIntent.getActivity(
                    context, id, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, pi)

                // 2. EMPTY STATE LOGIC
                if (cities.isEmpty()) {
                    views.setViewVisibility(R.id.tv_empty_state, View.VISIBLE)
                    views.setViewVisibility(R.id.layout_content, View.GONE)
                } else {
                    views.setViewVisibility(R.id.tv_empty_state, View.GONE)
                    views.setViewVisibility(R.id.layout_content, View.VISIBLE)

                    // 3. CITY 1 DATA
                    val city1 = cities[0]
                    val cleanName1 = city1.cityName.split(" (")[0].uppercase()
                    views.setTextViewText(R.id.tv_city_1, cleanName1)

                    // Professional Way: TextClock ko timezone assign karein taaki wo khud tick kare
                    views.setString(R.id.tv_time_1, "setTimeZone", city1.timeZoneId)

                    // 4. CITY 2 DATA (If exists, else show Local)
                    if (count > 1) {
                        val city2 = cities[1]
                        val cleanName2 = city2.cityName.split(" (")[0].uppercase()
                        views.setTextViewText(R.id.tv_city_2, cleanName2)
                        views.setString(R.id.tv_time_2, "setTimeZone", city2.timeZoneId)
                    } else {
                        views.setTextViewText(R.id.tv_city_2, "LOCAL")
                        views.setString(R.id.tv_time_2, "setTimeZone", TimeZone.getDefault().id)
                    }
                }

                // Update the widget
                appWidgetManager.updateAppWidget(id, views)
            }
        }
    }

    companion object {
        fun refreshWidget(context: Context) {
            val intent = Intent(context, WidgetDualStacked::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, WidgetDualStacked::class.java)
            )

            if (ids.isNotEmpty()) {
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                context.sendBroadcast(intent)
            }
        }
    }
}