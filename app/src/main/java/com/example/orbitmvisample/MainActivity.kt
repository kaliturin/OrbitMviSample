package com.example.orbitmvisample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.orbitmvisample.di.MviKoinModule
import com.example.orbitmvisample.ui.main.MainFragment
import timber.log.Timber

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // init Timber
        Timber.plant(Timber.DebugTree())

        MviKoinModule.startKoin(application)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment())
                .commitNow()
        }
    }
}