package com.example.orbitmvisample.experimental

import androidx.lifecycle.ViewModel
import com.appmattus.layercache.Cache
import com.example.orbitmvisample.apierrorhandler.ApiErrorHandler
import com.example.orbitmvisample.apierrorhandler.ApiException
import com.example.orbitmvisample.fetcher.*
import kotlinx.coroutines.*
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import timber.log.Timber

/**
 * VM with data fetching service cache
 */
open class FetcherCacheServiceViewModel<T : Any>(
    fetcherService: FetcherService<T>,
    cacheService: Cache<Any, T>,
    private val errorHandler: ApiErrorHandler? = null,
) : ViewModel(), ContainerHost<Response<T>, Nothing> {

    override val container = container<Response<T>, Nothing>(Response.NoNewData())

    private val cacheService = cacheService.asFetcherServiceCache(fetcherService)

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
     * Sends request to VM to start for emitting [Response] states
     * @param arguments arguments of [FetcherService]
     * @param cleanCache if true - then removes a value from the cache before fetching it from [FetcherService]
     */
    @Suppress("UNCHECKED_CAST")
    fun request(
        arguments: FetcherArguments<T>,
        cleanCache: Boolean = false
    ) = intent {

        // build response info
        val info = ResponseInfo(
            origin = ResponseOrigin.Cache,
            responseId = nextResponseId(),
            arguments = arguments
        )

        // clear cache if required
        if (cleanCache) cleanCache(arguments)

        // get a value from the cache
        val value = try {
            cacheService.get(arguments)
        } catch (e: Exception) {
            Timber.e(e, "Error on cache value getting with key=$arguments")
            // convert the exception to ApiException and handle it by default
            val apiException = errorHandler?.handle(e) ?: ApiException(cause = e)
            // then state of response with the error on data fetching
            reduce {
                Response.Error.Exception(apiException, info)
            }
            return@intent
        }

        // state of response with data from the cache
        reduce {
            if (value != null)
                Response.Data(info, value)
            else
                Response.NoNewData(info)
        }
    }

    suspend fun cleanCache(arguments: FetcherArguments<T>) {
        try {
            cacheService.evict(arguments)
        } catch (e: Exception) {
            Timber.e(e, "Error on cache clearing with key=$arguments")
        }
    }
}