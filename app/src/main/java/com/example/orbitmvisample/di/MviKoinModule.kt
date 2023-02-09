package com.example.orbitmvisample.di

import android.app.Application
import com.example.orbitmvisample.fetcher.CacheServiceBuilder
import com.example.orbitmvisample.fetcher.CacheSettings
import com.example.orbitmvisample.service.IntCatchingFetcherService
import com.example.orbitmvisample.service.IntFetcherService
import com.example.orbitmvisample.vm.IntViewModel
import com.example.orbitmvisample.vm.IntViewModel2
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.qualifier.named
import org.koin.dsl.module

object MviKoinModule {

    fun startKoin(application: Application) {
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(application)
            modules(listOf(module()))
        }
    }

    private fun module() = module {

        single(named(SHORT_TERM_CACHE_SETTINGS)) {
            CacheSettings(timeToExpire = 10, capacity = 10)
        }

        single(named(SHORT_TERM_CACHE)) {
            CacheServiceBuilder(get(named(SHORT_TERM_CACHE_SETTINGS))).build()
        }

        single { IntFetcherService() }

        single { IntCatchingFetcherService(get(), get(named(SHORT_TERM_CACHE))) }

        viewModel { IntViewModel(get(), get(named(SHORT_TERM_CACHE))) }

        viewModel { IntViewModel2(get()) }
    }

    private const val SHORT_TERM_CACHE_SETTINGS = "ShortTermCacheSettings"
    private const val SHORT_TERM_CACHE = "ShortTermCache"
}