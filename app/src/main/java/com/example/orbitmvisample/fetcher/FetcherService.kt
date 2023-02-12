package com.example.orbitmvisample.fetcher

/**
 * Some data fetching service
 */
interface FetcherService<T> {
    fun name(): String? = javaClass.canonicalName
    suspend fun request(arguments: FetcherArguments<T>?): T
}