package com.example.orbitmvisample.cache

import com.example.orbitmvisample.fetcher.FetcherArguments

/**
 * Makes sure that a cache key is unique by adding a cache owner name.
 * This may be needed to share the cache between multiple services.
 */
class CacheKeyBuilder(private val cacheOwner: String? = null) {
    fun getCacheKey(arguments: FetcherArguments<*>?): Any? {
        return getCacheKey(cacheOwner, arguments)
    }

    fun getCacheKey(cacheOwner: String?, arguments: FetcherArguments<*>?): Any? {
        return arguments?.getCacheKey()?.let {
            listOf(cacheOwner, it)
        }
    }
}