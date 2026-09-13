package com.nzdeveloper.androidclock.alarmtimer.stopwatch.service

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.R
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.activity.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL


class WidgetWeatherClock : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) {
            updateWeather(context, manager, id)
        }
    }

    private fun updateWeather(context: Context, manager: AppWidgetManager, id: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_weather_clock)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Lahore ke coordinates
                val lat = 31.5204
                val lon = 74.3587

                // Open-Meteo API (No Key Needed!)
                val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true"
                val response = URL(url).readText()
                val json = JSONObject(response)
                val current = json.getJSONObject("current_weather")

                val temp = current.getDouble("temperature").toInt()
                val weatherCode = current.getInt("weathercode")

                withContext(Dispatchers.Main) {
                    views.setTextViewText(R.id.tv_temperature, "$temp°C")

                    // 0 = Clear, 1-3 = Partly Cloudy, 45-48 = Fog, 51-67 = Rain
                    val iconRes = when (weatherCode) {
                        0 -> R.drawable.ic_weather_sun
                        in 1..3 -> R.drawable.ic_weather_cloud
                        in 51..67 -> R.drawable.ic_weather_rain
                        else -> R.drawable.ic_weather_cloud
                    }
                    views.setImageViewResource(R.id.iv_weather_icon, iconRes)

                    manager.updateAppWidget(id, views)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}









//class WidgetWeatherClock : AppWidgetProvider() {
//    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
//        for (id in ids) {
//            val views = RemoteViews(context.packageName, R.layout.widget_weather_clock)
//
//            // Click to open App
//            val intent = Intent(context, MainActivity::class.java)
//            val pi = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_IMMUTABLE)
//            views.setOnClickPendingIntent(R.id.widget_root, pi)
//
//            // Placeholder Weather Update
//            views.setTextViewText(R.id.tv_temperature, "24°C")
//
//            manager.updateAppWidget(id, views)
//        }
//    }
//}