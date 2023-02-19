package com.example.orbitmvisample.cache

import com.example.orbitmvisample.fetcher.FetcherArguments
import com.example.orbitmvisample.fetcher.FetcherService

/**
 * Makes sure that a cache key is unique by adding [FetcherService] name.
 * This may be needed to share the cache between multiple services.
 */
class CacheKeyBuilder<T>(private val fetcherService: FetcherService<T>? = null) {
    fun getCacheKey(arguments: FetcherArguments<T>?): Any? {
        return getCacheKey(fetcherService?.name(), arguments)
    }

    fun getCacheKey(cacheOwner: String?, arguments: FetcherArguments<T>?): Any? {
        return arguments?.getCacheKey()?.let {
            listOf(cacheOwner, it)
        }
    }
}