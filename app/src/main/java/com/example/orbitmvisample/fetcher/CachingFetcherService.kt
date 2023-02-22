package com.example.orbitmvisample.fetcher

import com.appmattus.layercache.Cache
import com.example.orbitmvisample.cache.CacheKeyBuilder
import timber.log.Timber

open class CachingFetcherService<T : Any>(
    private val fetcherService: FetcherService<T>,
    private val cacheService: Cache<Any, Any>?
) : FetcherService<T> {

    override fun name(): String? = fetcherService.name()

    private val cacheKeyBuilder = CacheKeyBuilder(fetcherService)

    @Suppress("UNCHECKED_CAST")
    override suspend fun request(arguments: FetcherArguments<T>?): T {
        val key = cacheKeyBuilder.getCacheKey(arguments)

        // get a value from the cache
        if (key != null) {
            val value = try {
                cacheService?.get(key)
            } catch (e: Exception) {
                Timber.e(e, "Error on cache value getting with key=$key")
            }
            (value as? T)?.let { return it }
        }

        // request a value from fetcher
        val value = fetcherService.request(arguments)

        // put the value to the cache only if a cache key is provided
        if (key != null) {
            try {
                cacheService?.set(key, value)
            } catch (e: Exception) {
                Timber.e(e, "Error on cache value setting with key=$key")
            }
        }

        return value
    }

    suspend fun request(arguments: FetcherArguments<T>?, cleanCache: Boolean): T {
        if (cleanCache) cleanCache(arguments)
        return request(arguments)
    }

    suspend fun cleanCache(arguments: FetcherArguments<T>?) {
        cacheKeyBuilder.getCacheKey(arguments)?.let { cleanCache(it) }
    }

    suspend fun cleanCache(key: Any) {
        try {
            cacheService?.evict(key)
        } catch (e: Exception) {
            Timber.e(e, "Error on cache clearing with key=$key")
        }
    }
}