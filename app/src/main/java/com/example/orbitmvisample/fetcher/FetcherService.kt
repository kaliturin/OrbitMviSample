package com.example.orbitmvisample.fetcher

import com.appmattus.layercache.Cache

/**
 * Some data fetching service
 */
interface Fetcher<T : Any> : Cache<FetcherArguments<T>, T> {
    override suspend fun get(key: FetcherArguments<T>): T?

    override suspend fun evict(key: FetcherArguments<T>) {}

    override suspend fun evictAll() {}

    override suspend fun set(key: FetcherArguments<T>, value: T) {}
}

interface FetcherService<T : Any> : Fetcher<T> {
    suspend fun request(arguments: FetcherArguments<T>): T?

    override suspend fun get(key: FetcherArguments<T>): T? = request(key)
}