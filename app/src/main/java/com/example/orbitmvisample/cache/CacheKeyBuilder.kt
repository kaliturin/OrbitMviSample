package com.example.orbitmvisample.cache

import com.example.orbitmvisample.fetcher.FetcherArguments

/**
 * Makes sure that a cache key is unique by adding a cache owner name.
 * This may be needed to share the cache between multiple services.
 */
interface CacheKeyBuilder {
    fun build(arguments: FetcherArguments<*>): Any
    fun build(cacheOwner: String?, arguments: FetcherArguments<*>): Any
}

class CacheKeyBuilderDefault(private val cacheOwner: String?) : CacheKeyBuilder {
    override fun build(arguments: FetcherArguments<*>): Any {
        return build(cacheOwner, arguments)
    }

    override fun build(cacheOwner: String?, arguments: FetcherArguments<*>): Any {
        val key = arguments.getCacheKey()
        return cacheOwner?.let { listOf(it, key) } ?: key
    }
}