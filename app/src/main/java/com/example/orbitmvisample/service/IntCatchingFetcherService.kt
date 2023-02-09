package com.example.orbitmvisample.service

import com.appmattus.layercache.Cache
import com.example.orbitmvisample.fetcher.CachingFetcherService

class IntCatchingFetcherService(
    fetcherService: IntFetcherService,
    cacheService: Cache<Any, Any>
) : CachingFetcherService<Int>(fetcherService, cacheService)