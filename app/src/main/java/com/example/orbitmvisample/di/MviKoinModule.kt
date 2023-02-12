package com.example.orbitmvisample.di

import android.app.Application
import com.example.orbitmvisample.apierrorhandler.ApiErrorHandler
import com.example.orbitmvisample.apierrorhandler.ApiExceptionBuilder
import com.example.orbitmvisample.apierrorhandler.impl.ApiErrorHandlerImpl
import com.example.orbitmvisample.apierrorhandler.impl.ApiErrorPropagator
import com.example.orbitmvisample.apierrorhandler.impl.ApiExceptionBuilderImpl
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

        single(named(SHORT_TERM_CACHE)) {
            val settings = CacheSettings(timeToExpire = 10, capacity = 100)
            CacheServiceBuilder(settings).build()
        }

        single { IntFetcherService() }

        single { IntCatchingFetcherService(get(), get(named(SHORT_TERM_CACHE))) }

        single<ApiExceptionBuilder> { ApiExceptionBuilderImpl(androidContext().resources) }

        single<ApiErrorHandler>(named<ApiErrorPropagator>()) { ApiErrorPropagator() }

        single<ApiErrorHandler>(named<ApiErrorHandlerImpl>()) {
            ApiErrorHandlerImpl(get(), get(named<ApiErrorPropagator>()))
        }

        viewModel {
            IntViewModel(
                get(),
                get(named<ApiErrorHandlerImpl>()),
                get(named(SHORT_TERM_CACHE))
            )
        }

        viewModel {
            IntViewModel2(
                get(),
                get(named<ApiErrorHandlerImpl>())
            )
        }
    }

    private const val SHORT_TERM_CACHE = "SHORT_TERM_CACHE"
}