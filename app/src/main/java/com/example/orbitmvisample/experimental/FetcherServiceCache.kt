package com.example.orbitmvisample.experimental

import com.appmattus.layercache.Cache
import com.appmattus.layercache.Fetcher
import com.example.orbitmvisample.cache.CacheKeyBuilderAny
import com.example.orbitmvisample.fetcher.FetcherArguments
import com.example.orbitmvisample.fetcher.FetcherService


fun <V : Any> Cache<Any, V>.asFetcherServiceCache(service: FetcherService<V>) =
    FetcherCacheWrapper(this, service.name())
        .compose(FetcherServiceCache(service))

private class FetcherCacheWrapper<V : Any>(
    private val cache: Cache<Any, V>,
    cacheOwner: String? = null,
) : Cache<FetcherArguments<V>, V> {

    private val cacheKeyBuilder = CacheKeyBuilderAny(cacheOwner)

    override suspend fun get(key: FetcherArguments<V>): V? {
        return cacheKeyBuilder.build(key)?.let { cache.get(it) }
    }

    override suspend fun set(key: FetcherArguments<V>, value: V) {
        cacheKeyBuilder.build(key)?.let { cache.set(it, value) }
    }

    override suspend fun evict(key: FetcherArguments<V>) {
        cacheKeyBuilder.build(key)?.let { cache.evict(it) }
    }

    override suspend fun evictAll() {
        cache.evictAll()
    }
}

private class FetcherServiceCache<V : Any>(private val service: FetcherService<V>) :
    Fetcher<FetcherArguments<V>, V> {

    override suspend fun get(key: FetcherArguments<V>): V {
        return service.request(key)
    }
}