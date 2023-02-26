package com.example.orbitmvisample.fetcher

/**
 * Some data fetching arguments
 */
interface FetcherArguments<T> {
    /**
     * Returns a cache key
     */
    fun getCacheKey(): Any = this

    /**
     * Returns true if the passed value is valid for caching
     */
    fun isCaching(value: T): Boolean = true
}

data class FetcherArgumentsDefault<T>(val name: String = "default") : FetcherArguments<T>