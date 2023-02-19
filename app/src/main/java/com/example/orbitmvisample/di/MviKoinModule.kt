package com.example.orbitmvisample.di

import android.app.Application
import com.example.orbitmvisample.apierrorhandler.ApiErrorHandler
import com.example.orbitmvisample.apierrorhandler.ApiExceptionBuilder
import com.example.orbitmvisample.apierrorhandler.impl.ApiErrorHandlerImpl
import com.example.orbitmvisample.apierrorhandler.impl.ApiErrorPropagator
import com.example.orbitmvisample.apierrorhandler.impl.ApiExceptionBuilderImpl
import com.example.orbitmvisample.cache.CacheBuilder
import com.example.orbitmvisample.cache.CacheManager
import com.example.orbitmvisample.cache.impl.CACHE_10_SEC
import com.example.orbitmvisample.cache.impl.CacheBuilderImpl
import com.example.orbitmvisample.cache.impl.defaultListOfCacheSettings
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
        // Cache builder
        single<CacheBuilder> { CacheBuilderImpl() }

        // Cache manager
        single {
            CacheManager(get()).addSettings(*defaultListOfCacheSettings.toTypedArray())
        }

        // Error handler
        single<ApiExceptionBuilder> { ApiExceptionBuilderImpl(androidContext().resources) }

        single<ApiErrorHandler>(named<ApiErrorPropagator>()) { ApiErrorPropagator() }

        single<ApiErrorHandler>(named<ApiErrorHandlerImpl>()) {
            ApiErrorHandlerImpl(get(), get(named<ApiErrorPropagator>()))
        }

        // Services impl
        single { IntFetcherService() }

        single {
            IntCatchingFetcherService(
                get(),
                get<CacheManager>().getCache(CACHE_10_SEC)
            )
        }

        // ViewModels impl
        viewModel {
            IntViewModel(
                get(),
                get(named<ApiErrorHandlerImpl>()),
                get<CacheManager>().getCache(CACHE_10_SEC)
            )
        }

        viewModel {
            IntViewModel2(
                get(),
                get(named<ApiErrorHandlerImpl>())
            )
        }
    }
}