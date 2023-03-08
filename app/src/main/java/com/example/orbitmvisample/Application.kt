package com.example.orbitmvisample

import android.content.Context
import androidx.multidex.MultiDexApplication
import com.example.orbitmvisample.di.KoinModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class Application : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@Application)
            modules(listOf(KoinModule.module()))
        }
    }

    init {
        appContext = this
    }

    companion object {
        lateinit var appContext: Application
            private set
    }
}

val AppContext: Context
    get() = Application.appContext