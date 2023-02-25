package com.example.orbitmvisample.vm

import com.appmattus.layercache.Cache
import com.example.orbitmvisample.apierrorhandler.ApiErrorHandler
import com.example.orbitmvisample.fetcher.FetcherViewModel
import com.example.orbitmvisample.service.IntFetcherService

class IntViewModel(
    fetcherService: IntFetcherService,
    errorHandler: ApiErrorHandler,
    cacheService: Cache<Any, Int>
) : FetcherViewModel<Int>(fetcherService, errorHandler, cacheService)