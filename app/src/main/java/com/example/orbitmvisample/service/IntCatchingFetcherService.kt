package com.example.orbitmvisample.service

import com.appmattus.layercache.Cache
import com.example.orbitmvisample.fetcher.CachingFetcherService
import com.example.orbitmvisample.fetcher.FetcherService

class IntCatchingFetcherService(
    fetcherService: FetcherService<Int>,
    cacheService: Cache<Any, Any>
) : CachingFetcherService<Int>(fetcherService, cacheService)