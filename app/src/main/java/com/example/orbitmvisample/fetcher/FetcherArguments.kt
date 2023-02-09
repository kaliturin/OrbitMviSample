package com.example.orbitmvisample.fetcher

/**
 * Some data fetching arguments
 */
interface FetcherArguments {
    fun getCacheKey(): Any? = null
}