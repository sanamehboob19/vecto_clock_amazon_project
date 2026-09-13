package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.activity.widget

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.R
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.widget.FrameLayout
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.databinding.ActivityWidgetHubBinding
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.service.ClockWidget
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.service.WidgetAnalog
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.service.WidgetDigitalStacked
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.service.WidgetDualStacked
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.service.WidgetGlassDigital
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.service.WidgetMinimalCircle
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.service.WidgetPinkAnalog
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.service.WidgetPinkDigital
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.service.WidgetStopwatch
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.service.WidgetWeatherClock
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.service.WidgetWorldClock
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.base.BaseActivity
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.ToastUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlin.jvm.java


@AndroidEntryPoint
class WidgetHubActivity :
    BaseActivity<ActivityWidgetHubBinding>(ActivityWidgetHubBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar()
        setupWidgetGallery()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }
    }

    /**
     * Populates the gallery cards with titles, descriptions, and logic.
     */
    private fun setupWidgetGallery() {
        // 1. Stacked Digital
        binding.cardStyleDigital.apply {
            tvWidgetStyleName.text = "Stacked Digital"
            tvWidgetSize.text = "Canvas: 2x2"

            loadRealWidgetPreview(widgetPreviewContainer, R.layout.widget_digital_stacked)

            btnAddWidget.setOnClickListener {
                requestToPinWidget(WidgetDigitalStacked::class.java, R.drawable.preview_stacked)
            }
        }

//        // 2. Classic Analog
        binding.cardStyleAnalog.apply {
            tvWidgetStyleName.text = "Classic Analog"
            tvWidgetSize.text = "Canvas: 2x2"

            loadRealWidgetPreview(widgetPreviewContainer, R.layout.widget_analog)

            btnAddWidget.setOnClickListener {
                requestToPinWidget(WidgetAnalog::class.java, R.drawable.preview_analog)
            }
        }

        // 3. World Clock
        binding.cardStyleWorld.apply {
            tvWidgetStyleName.text = "World Clock Grid"
            tvWidgetSize.text = "Canvas: 4x2"

            loadRealWidgetPreview(widgetPreviewContainer, R.layout.widget_world_clock_grid)

            btnAddWidget.setOnClickListener {
                requestToPinWidget(WidgetWorldClock::class.java, R.drawable.preview_bar)
            }
        }

        // 4. Stopwatch
        binding.cardStyleStopwatch.apply {
            tvWidgetStyleName.text = "Focus Stopwatch"
            tvWidgetSize.text = "Canvas: 2x1"

            loadRealWidgetPreview(widgetPreviewContainer, R.layout.widget_stopwatch)

            btnAddWidget.setOnClickListener {
                requestToPinWidget(WidgetStopwatch::class.java, R.drawable.preview_stopwatch)
            }
        }

//        // 5. Style: Clock & Date Bar (The Toolbar style we made first)
        binding.cardStyleBar.apply {
            tvWidgetStyleName.text = "Clock & Date Bar"
            tvWidgetSize.text = "Canvas: 4x1" // Toolbar is usually 4 columns wide

            loadRealWidgetPreview(widgetPreviewContainer, R.layout.widget_clock_standard)

            btnAddWidget.setOnClickListener {
                requestToPinWidget(ClockWidget::class.java, R.drawable.preview_bar)
            }
        }

        binding.pinkAnalogWidget.apply {
            tvWidgetStyleName.text = "Pink Glow Analog"
            tvWidgetSize.text = "Canvas: 2x2"
            loadRealWidgetPreview(widgetPreviewContainer, R.layout.widget_pink_analog)

            btnAddWidget.setOnClickListener {
                requestToPinWidget(
                    WidgetPinkAnalog::class.java,
                    R.drawable.preview_pink_analog
                )
            }
        }

        binding.pinkDigitalWidget.apply {
            tvWidgetStyleName.text = "Neon Pink Digital"
            tvWidgetSize.text = "Canvas: 4x2"
            loadRealWidgetPreview(widgetPreviewContainer, R.layout.widget_pink_digital)

            btnAddWidget.setOnClickListener {
                requestToPinWidget(
                    WidgetPinkDigital::class.java,
                    R.drawable.preview_pink_digital
                )
            }
        }


        binding.glassWidget.apply {
            tvWidgetStyleName.text = "Glassmorphism Aesthetics"
            tvWidgetSize.text = "Canvas: 4x2"
            loadRealWidgetPreview(widgetPreviewContainer, R.layout.widget_glass_digital)
            btnAddWidget.setOnClickListener {
                requestToPinWidget(WidgetGlassDigital::class.java, R.drawable.bg_glass_mesh)
            }
        }


        binding.minimalDotWidget.apply {
            tvWidgetStyleName.text = "Minimalist Dot Circle"
            tvWidgetSize.text = "Canvas: 2x2"
            loadRealWidgetPreview(widgetPreviewContainer, R.layout.widget_minimal_circle)
            btnAddWidget.setOnClickListener {
                requestToPinWidget(WidgetMinimalCircle::class.java, R.drawable.preview_minimal_circle)
            }
        }


        binding.dualStackedWidget.apply {
            tvWidgetStyleName.text = "Dual-Time Stacked"
            tvWidgetSize.text = "Canvas: 4x2"
            loadRealWidgetPreview(widgetPreviewContainer, R.layout.widget_dual_stacked)
            btnAddWidget.setOnClickListener {
                requestToPinWidget(WidgetDualStacked::class.java, R.drawable.preview_dual_stacked)
            }
        }

        binding.weatherClockWidget.apply {
            tvWidgetStyleName.text = "Weather + Clock"
            tvWidgetSize.text = "Canvas: 4x2"
            loadRealWidgetPreview(widgetPreviewContainer, R.layout.widget_weather_clock)
            btnAddWidget.setOnClickListener {
                requestToPinWidget(WidgetWeatherClock::class.java, R.drawable.preview_weather_clock)
            }
        }



    }

    /**
     * Professional Method: Triggers the native Android "Add to Home Screen" dialog.
     * This API works on Android 8.0 (Oreo) and above.
     */
    private fun requestToPinWidget(widgetClass: Class<*>, previewDrawableId: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val appWidgetManager = getSystemService(AppWidgetManager::class.java)
        val myProvider = ComponentName(this, widgetClass)

        if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported) {

            // 1. Create RemoteViews using the "Image Only" layout
            val remotePreview = RemoteViews(packageName, R.layout.layout_widget_image_preview)

            // 2. Set the specific preview image
            remotePreview.setImageViewResource(R.id.iv_final_preview, previewDrawableId)

            val extras = Bundle()
            extras.putParcelable(AppWidgetManager.EXTRA_APPWIDGET_PREVIEW, remotePreview)

            val successCallback = PendingIntent.getBroadcast(
                this, System.currentTimeMillis().toInt(), Intent(this, widgetClass),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            appWidgetManager.requestPinAppWidget(myProvider, extras, successCallback)
        }
    }


    private fun loadRealWidgetPreview(container: FrameLayout, layoutId: Int) {
        container.removeAllViews()

        val widgetView = layoutInflater.inflate(layoutId, container, false)

        widgetView.scaleX = 0.8f
        widgetView.scaleY = 0.8f

        container.addView(widgetView)
    }


}