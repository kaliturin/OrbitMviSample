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
import com.example.orbitmvisample.cache.impl.defaultListOfCacheSettings
import com.example.orbitmvisample.experimental.MultiIntFetcherService
import com.example.orbitmvisample.fetcher.FetcherViewModel
import com.example.orbitmvisample.fetcher.withLayerCache
import com.example.orbitmvisample.service.IntFetcherService
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
            //val persistCache = get<CacheManager>().get<Int>(CACHE_15_SEC)
            memoryCache//!! + persistCache!!
        }

        // Error handler
        single<ApiExceptionBuilder> { ApiExceptionBuilderImpl(androidContext().resources) }
        single<ApiErrorHandler> { ApiErrorHandlerImpl(get(), ApiErrorPropagator()) }

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