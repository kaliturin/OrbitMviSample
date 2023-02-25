package com.example.orbitmvisample.experimental

import com.appmattus.layercache.Cache
import com.example.orbitmvisample.fetcher.FetcherService

class IntCatchingService(
    fetcherService: FetcherService<Int>,
    cacheService: Cache<Any, Any>?
) : CachingFetcherService<Int>(fetcherService, cacheService)