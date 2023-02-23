package com.example.orbitmvisample.di

import android.app.Application
import com.example.orbitmvisample.apierrorhandler.ApiErrorHandler
import com.example.orbitmvisample.apierrorhandler.ApiExceptionBuilder
import com.example.orbitmvisample.apierrorhandler.impl.ApiErrorHandlerImpl
import com.example.orbitmvisample.apierrorhandler.impl.ApiErrorPropagator
import com.example.orbitmvisample.apierrorhandler.impl.ApiExceptionBuilderImpl
import com.example.orbitmvisample.cache.CacheManager
import com.example.orbitmvisample.cache.crypto.CryptoManager
import com.example.orbitmvisample.cache.crypto.CryptoManagerImpl
import com.example.orbitmvisample.cache.impl.CACHE_10_SEC
import com.example.orbitmvisample.cache.impl.Cache2KBuilder
import com.example.orbitmvisample.cache.impl.PreferencesCacheManager
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

        single<CryptoManager> { CryptoManagerImpl() }

        // Persistent cache manager
        single { PreferencesCacheManager(androidContext(), get()) }

        // In memory cache manager
        single {
            CacheManager(Cache2KBuilder(), *defaultListOfCacheSettings.toTypedArray())
        }

        // Error handler
        single<ApiExceptionBuilder> { ApiExceptionBuilderImpl(androidContext().resources) }
        single<ApiErrorHandler> { ApiErrorHandlerImpl(get(), ApiErrorPropagator()) }

        // Services impl
        single { IntFetcherService() }
        single {
            IntCatchingFetcherService(get(), get<CacheManager>().get(CACHE_10_SEC))
        }

        // ViewModels impl
        viewModel {
            IntViewModel(get(), get(), get<CacheManager>().get(CACHE_10_SEC))
        }
        viewModel { IntViewModel2(get(), get()) }
    }
}