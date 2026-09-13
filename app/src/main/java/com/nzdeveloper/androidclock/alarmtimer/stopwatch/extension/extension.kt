package com.nzdeveloper.androidclock.alarmtimer.stopwatch.extension

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.service.WidgetDualStacked

fun Context.getActivity(): AppCompatActivity? {
    var curr = this
    while (curr is ContextWrapper) {
        if (curr is AppCompatActivity) return curr
        curr = curr.baseContext
    }
    return null
}



fun notifyDualWidgetUpdate(context: Context) {
    val intent = Intent(context, WidgetDualStacked::class.java).apply {
        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
    }
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val ids = appWidgetManager.getAppWidgetIds(
        ComponentName(
            context,
            WidgetDualStacked::class.java
        )
    )

    if (ids.isNotEmpty()) {
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }
}