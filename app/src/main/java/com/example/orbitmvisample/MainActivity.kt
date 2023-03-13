package com.example.orbitmvisample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.orbitmvisample.ui.alert.impl.FloatingToastAlertBuilder
import com.example.orbitmvisample.ui.main.MainFragment
import com.example.orbitmvisample.utils.NetworkAccessibilityObserver
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val networkAccessibilityObserver: NetworkAccessibilityObserver by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment())
                .commitNow()
        }

        networkAccessibilityObserver.setAlertBuilder(
            FloatingToastAlertBuilder(window.decorView.rootView)
        ).observe()
    }
}