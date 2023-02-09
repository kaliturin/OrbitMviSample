package com.example.orbitmvisample.vm

import com.appmattus.layercache.Cache
import com.example.orbitmvisample.fetcher.FetcherViewModel
import com.example.orbitmvisample.service.IntFetcherService

class IntViewModel(
    fetcherService: IntFetcherService,
    cacheService: Cache<Any, Any>? = null
) : FetcherViewModel<Int>(fetcherService, cacheService)