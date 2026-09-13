package com.nzdeveloper.androidclock.alarmtimer.stopwatch.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.util.TinyDB
import javax.inject.Inject


abstract class BaseActivity<VB : ViewBinding>(
    private val inflate: (LayoutInflater) -> VB
) : AppCompatActivity()
{

    lateinit var binding: VB

    @Inject
    lateinit var tinyDB: TinyDB // Hilt provides this to every Activity now


    override fun onCreate(savedInstanceState: Bundle?) {
        // call super.onCreate so Hilt injects tinyDB
        super.onCreate(savedInstanceState)

        binding = inflate(layoutInflater)
        setContentView(binding.root)

    }




    // Call this function in any Activity that has a back button
    // eg    setupBackButton(R.id.btnBack) { anything }
    fun setupBackButton(backButtonId: Int, onBackClick: (() -> Unit)? = null) {
        findViewById<View>(backButtonId)?.setOnClickListener {
            if (onBackClick != null) {
                // If you provided custom code, run it
                onBackClick()
            } else {
                // Otherwise, just do the standard back press
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }



}