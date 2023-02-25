package com.example.orbitmvisample.di

import android.app.Application
import com.example.orbitmvisample.apierrorhandler.ApiErrorHandler
import com.example.orbitmvisample.apierrorhandler.ApiExceptionBuilder
import com.example.orbitmvisample.apierrorhandler.impl.ApiErrorHandlerImpl
import com.example.orbitmvisample.apierrorhandler.impl.ApiErrorPropagator
import com.example.orbitmvisample.apierrorhandler.impl.ApiExceptionBuilderImpl
import com.example.orbitmvisample.cache.CacheBuilderProvider
import com.example.orbitmvisample.cache.CacheManager
import com.example.orbitmvisample.cache.impl.CACHE_10_SEC
import com.example.orbitmvisample.cache.impl.CACHE_30_SEC
import com.example.orbitmvisample.cache.impl.defaultListOfCacheSettings
import com.example.orbitmvisample.experimental.IntCatchingService
import com.example.orbitmvisample.service.IntFetcherService
import com.example.orbitmvisample.vm.IntViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.qualifier.named
import org.koin.dsl.module

typealias MemoryCacheManager = CacheManager
typealias PreferencesCacheManager = CacheManager

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

        // In memory cache manager
        single {
            MemoryCacheManager(
                cacheBuilderProvider = get(),
                settings = defaultListOfCacheSettings.toTypedArray()
            )
        }

        // Persistent cache manager
        single {
            PreferencesCacheManager(
                cacheBuilderProvider = get(),
                settings = defaultListOfCacheSettings.toTypedArray()
            )
        }

        single(named("layerCache")) {
            val memoryCache = get<MemoryCacheManager>().get<Int>(CACHE_10_SEC)
            val persistCache = get<PreferencesCacheManager>().get<Int>(CACHE_30_SEC)
            memoryCache!! + persistCache!!
        }

        // Error handler
        single<ApiExceptionBuilder> { ApiExceptionBuilderImpl(androidContext().resources) }
        single<ApiErrorHandler> { ApiErrorHandlerImpl(get(), ApiErrorPropagator()) }

        // Services impl
        single { IntFetcherService() }
        single {
            IntCatchingService(get(), get<CacheManager>().get(CACHE_10_SEC))
        }

        // ViewModels impl
        viewModel {
            IntViewModel(get(), get(), get(named("layerCache")))
        }
    }
}