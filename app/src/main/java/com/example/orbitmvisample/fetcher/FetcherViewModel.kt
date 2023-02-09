package com.example.orbitmvisample.fetcher

import androidx.lifecycle.ViewModel
import com.appmattus.layercache.Cache
import kotlinx.coroutines.*
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import timber.log.Timber

/**
 * Simple template VM with data fetching service and optional cache service
 */
open class FetcherViewModel<T>(
    private val fetcherService: FetcherService<T>,
    private val cacheService: Cache<Any, Any>? = null
) : ViewModel(), ContainerHost<Response<T>, Nothing> {

    override val container =
        container<Response<T>, Nothing>(Response.NoNewData())

    /**
     * If false and in case of using data class of [FetcherArguments] implementation, then the VM
     * won't respond with the same states as it is conventional for [kotlinx.coroutines.flow.StateFlow]
     */
    var withResponseId: Boolean = true

    private var requestCounter: Long = 0

    private fun nextResponseId(): Long {
        return if (withResponseId) ++requestCounter else 0
    }

    /**
     * Makes sure that a cache key is unique by adding [FetcherService] name.
     * This could be needed in case of sharing a cache service between several services.
     */
    open fun getCacheKey(arguments: FetcherArguments?): Any? {
        return arguments?.getCacheKey()?.let {
            listOf(fetcherService.name(), it)
        }
    }

    /**
     * Sends request to VM to start for emitting [Response] states
     * @param arguments arguments of [FetcherService]
     * @param clearCache if true - then removes a value from the cache before fetching it from [FetcherService]
     */
    @Suppress("UNCHECKED_CAST")
    fun request(
        arguments: FetcherArguments? = null,
        clearCache: Boolean = false
    ) = intent {

        // build response info
        val info = ResponseInfo(responseId = nextResponseId(), arguments = arguments)

        // build cache key only if cache service is provided
        val key = cacheService?.let { getCacheKey(arguments) }

        if (key != null) {
            // clear cache if required
            if (clearCache) {
                try {
                    cacheService?.evict(key)
                } catch (e: Exception) {
                    Timber.e(e, "Error on cache clearing with key=$key")
                }
            } else {
                // get a value from the cache
                val value = try {
                    cacheService?.get(key)
                } catch (e: Exception) {
                    Timber.e(e, "Error on cache value getting with key=$key")
                }
                (value as? T)?.let {
                    // state of response with data from the cache
                    reduce { Response.Data(info.copy(origin = ResponseOrigin.Cache), it) }
                    return@intent
                }
            }
        }

        val isAlreadyLoading = state is Response.Loading

        // state of response about loading is started
        reduce { Response.Loading(info.copy(origin = ResponseOrigin.Fetcher)) }

        // if loading was already started - don't fetch again
        if (isAlreadyLoading) return@intent

        // request a value from fetcher
        val value = try {
            fetcherService.request(arguments)
        } catch (e: Exception) {
            Timber.e(e, "Error on requesting to fetcher with args=$arguments")
            // state of response with error data fetching
            reduce { Response.Error.Exception(e, info.copy(origin = ResponseOrigin.Fetcher)) }
            return@intent
        }

        // put the value to the cache only if a cache key is provided
        if (key != null) {
            try {
                cacheService?.set(key, value as Any)
            } catch (e: Exception) {
                Timber.e(e, "Error on cache value setting with key=$key")
            }
        }

        // state of response with data from the fetcher
        reduce { Response.Data(info.copy(origin = ResponseOrigin.Fetcher), value) }
    }
}