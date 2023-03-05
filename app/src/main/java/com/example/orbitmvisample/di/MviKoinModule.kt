package com.example.orbitmvisample.di

import android.app.Application
import com.example.orbitmvisample.apierrorhandler.AppErrorHandler
import com.example.orbitmvisample.apierrorhandler.AppExceptionBuilder
import com.example.orbitmvisample.apierrorhandler.impl.AppErrorHandlerDispatcher
import com.example.orbitmvisample.apierrorhandler.impl.AppErrorHandlerPropagator
import com.example.orbitmvisample.apierrorhandler.impl.AppExceptionBuilderImpl
import com.example.orbitmvisample.cache.CacheBuilderProvider
import com.example.orbitmvisample.cache.CacheManager
import com.example.orbitmvisample.cache.impl.CACHE_10_SEC
import com.example.orbitmvisample.cache.impl.CACHE_15_SEC
import com.example.orbitmvisample.cache.impl.defaultListOfCacheSettings
import com.example.orbitmvisample.eventbus.EventBusManager
import com.example.orbitmvisample.experimental.MultiIntFetcherService
import com.example.orbitmvisample.fetcher.FetcherViewModel
import com.example.orbitmvisample.fetcher.withLayerCache
import com.example.orbitmvisample.service.IntFetcherService
import com.example.orbitmvisample.ui.alert.AlertManager
import com.example.orbitmvisample.ui.alert.impl.DialogAlertBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.qualifier.named
import org.koin.dsl.module

typealias IntViewModel = FetcherViewModel<Int>

object MviKoinModule {

    fun startKoin(application: Application) {
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(application)
            modules(listOf(module()))
        }
    }

    private fun module() = module {

        single { CacheBuilderProvider(androidContext()) }

        // Cache manager
        single {
            CacheManager(
                cacheBuilderProvider = get(),
                settings = defaultListOfCacheSettings.toTypedArray()
            )
        }

        single(named("layerCache")) {
            val memoryCache = get<CacheManager>().get<Int>(CACHE_10_SEC)
            val persistCache = get<CacheManager>().get<Int>(CACHE_15_SEC)
            memoryCache!! + persistCache!!
        }

        single { AlertManager(alertBuilder = DialogAlertBuilder()) }

        single { EventBusManager() }

        // Error handler
        single<AppExceptionBuilder> { AppExceptionBuilderImpl(androidContext().resources) }
        single<AppErrorHandler> {
            AppErrorHandlerDispatcher(get(), AppErrorHandlerPropagator(get(), get()))
        }

        // Services impl
        single { IntFetcherService() }

        single {
            val cache = get<CacheManager>().get<Int>(CACHE_10_SEC)!!
            val service1 = get<IntFetcherService>().withLayerCache(cache)
            val service2 = get<IntFetcherService>().withLayerCache(cache)
            MultiIntFetcherService(service1, service2)
        }

        // ViewModels impl
        viewModel {
            IntViewModel(get<IntFetcherService>(), get(), get(named("layerCache")))
        }
    }
}