package com.example.orbitmvisample.fetcher

import com.appmattus.layercache.Cache
import timber.log.Timber

open class CachingFetcherService<T>(
    private val fetcherService: FetcherService<T>,
    private val cacheService: Cache<Any, Any>
) : FetcherService<T> {

    override fun name(): String? = fetcherService.name()

    /**
     * Makes sure that a cache key is unique by adding [FetcherService] name.
     * This could be needed in case of sharing a cache service between several services.
     */
    open fun getCacheKey(arguments: FetcherArguments?): Any? {
        return arguments?.getCacheKey()?.let {
            listOf(name(), it)
        }
    }

    suspend fun request(arguments: FetcherArguments?, clearCache: Boolean): T {
        // clear cache if required
        if (clearCache) {
            val key = getCacheKey(arguments)
            if (key != null) {
                try {
                    cacheService.evict(key)
                } catch (e: Exception) {
                    Timber.e(e, "Error on cache clearing with key=$key")
                }
            }
        }
        return request(arguments)
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun request(arguments: FetcherArguments?): T {
        val key = getCacheKey(arguments)

        // get a value from the cache
        if (key != null) {
            val value = try {
                cacheService.get(key)
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
                cacheService.set(key, value as Any)
            } catch (e: Exception) {
                Timber.e(e, "Error on cache value getting with key=$key")
            }
        }

        return value
    }
}