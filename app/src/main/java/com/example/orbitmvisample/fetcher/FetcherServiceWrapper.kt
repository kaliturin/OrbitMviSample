package com.example.orbitmvisample.fetcher

import com.appmattus.layercache.Cache
import com.example.orbitmvisample.cache.CacheKeyBuilder
import com.example.orbitmvisample.cache.CacheKeyBuilderAny

/**
 * Adds the cache as a layer cache of the fetcher service
 */
fun <T : Any> Cache<Any, T>.asLayerCacheOfService(
    fetcherService: FetcherService<T>,
    cacheKeyBuilder: CacheKeyBuilder = CacheKeyBuilderAny(fetcherService::class.qualifiedName)
): Cache<FetcherArguments<T>, T> = keyTransform<FetcherArguments<T>> {
    cacheKeyBuilder.build(it)
}.compose(fetcherService)

/**
 * Builds [FetcherService]<V> with a layer cache
 */
fun <T : Any> FetcherService<T>.withLayerCache(
    cache: Cache<Any, T>,
    cacheKeyBuilder: CacheKeyBuilder = CacheKeyBuilderAny(this::class.qualifiedName)
): FetcherService<T> =
    FetcherServiceWrapper(cache.asLayerCacheOfService(this, cacheKeyBuilder))

private class FetcherServiceWrapper<T : Any>(
    private val cache: Cache<FetcherArguments<T>, T>
) : FetcherService<T> {
    override suspend fun request(arguments: FetcherArguments<T>): T? {
        return cache.get(arguments)
    }
}