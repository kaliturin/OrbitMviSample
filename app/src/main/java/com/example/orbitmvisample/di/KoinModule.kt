package com.example.orbitmvisample.di

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
import com.example.orbitmvisample.eventbus.Event
import com.example.orbitmvisample.eventbus.EventBusManager
import com.example.orbitmvisample.eventbus.EventTimeoutManager
import com.example.orbitmvisample.experimental.MultiIntFetcherService
import com.example.orbitmvisample.fetcher.FetcherViewModel
import com.example.orbitmvisample.fetcher.withLayerCache
import com.example.orbitmvisample.service.IntFetcherService
import com.example.orbitmvisample.ui.alert.AlertManager
import com.example.orbitmvisample.ui.alert.impl.DialogAlertBuilder
import com.example.orbitmvisample.utils.NetworkAccessibilityObserver
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

typealias IntViewModel = FetcherViewModel<Int>

object KoinModule {
    fun module() = module {

        // OkHttpClient
        single {
            OkHttpClient.Builder()
                .addInterceptor(
                    HttpLoggingInterceptor().apply {
                        setLevel(HttpLoggingInterceptor.Level.BODY)
                    })
                .build()
        }

        single { CacheBuilderProvider(androidContext()) }

        // Cache manager
        single {
            CacheManager(
                cacheBuilderProvider = get(),
                settings = defaultListOfCacheSettings.toTypedArray()
            )
        }

        // LayerCache
        single(named("layerCache")) {
            val memoryCache = get<CacheManager>().get<Int>(CACHE_10_SEC)
            val persistCache = get<CacheManager>().get<Int>(CACHE_15_SEC)
            memoryCache!! + persistCache!!
        }

        single {
            AlertManager(
                context = androidContext(),
                defAlertBuilder = DialogAlertBuilder()
            )
        }

        // Events
        single {
            EventTimeoutManager()
                .register<Event.TechnicalWorks>()
                .register<Event.UserNotAuthorized>()
        }

        single { EventBusManager(get()) }

        single { NetworkAccessibilityObserver(get()) }

        // Error handler
        single<AppExceptionBuilder> { AppExceptionBuilderImpl() }
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