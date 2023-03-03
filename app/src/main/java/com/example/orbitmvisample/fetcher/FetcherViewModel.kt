package com.example.orbitmvisample.fetcher

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appmattus.layercache.Cache
import com.example.orbitmvisample.apierrorhandler.AppErrorHandler
import com.example.orbitmvisample.apierrorhandler.AppException
import com.example.orbitmvisample.cache.CacheKeyBuilder
import com.example.orbitmvisample.cache.CacheKeyBuilderDefault
import kotlinx.coroutines.*
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * VM with data fetching service and optional cache service
 */
open class FetcherViewModel<T : Any>(
    private val fetcherService: FetcherService<T>,
    private val errorHandler: AppErrorHandler? = null,
    private val cacheService: Cache<Any, T>? = null,
    private var cacheKeyBuilder: CacheKeyBuilder = CacheKeyBuilderDefault(fetcherService::class.qualifiedName),
    cacheWarmUpOnInit: Boolean = true
) : ViewModel(), ContainerHost<Response<T>, Nothing> {

    override val container = container<Response<T>, Nothing>(Response.NoNewData())

    private val fetcherRequests = PendingRequests<T>()
    private val cacheRequests = PendingRequests<T>()
    private val withRequestId = AtomicBoolean(true)
    private val requestCounter = AtomicLong(0)
    private var errorHandlerSettings: Bundle? = null
    private var cancelPendingRequestsOnClearedVM = true

    /**
     * Injects settings for AppErrorHandler
     */
    fun errorHandlerSettings(settings: Bundle?) = apply {
        errorHandlerSettings = settings
    }

    /**
     * Cancels current pending requests
     */
    fun cancelPendingRequests() = apply {
        fetcherRequests.cancel()
    }

    /**
     * Allows to ignore the responses of the current pending requests but cache its results
     */
    fun ignorePendingRequests() = apply {
        fetcherRequests.ignore()
        cacheRequests.ignore()
    }

    fun cancelPendingRequestsOnClearedVM(value: Boolean) = apply {
        cancelPendingRequestsOnClearedVM = value
    }

    /**
     * Sends request to VM to start for emitting [Response] states
     * @param arguments arguments of [FetcherService]
     * @param cleanCache if true - then removes a value from the cache before fetching it from [FetcherService]
     * @param refreshCache if true - then after response from cache VM trying to fetch a value from [FetcherService]
     */
    fun request(
        arguments: FetcherArguments<T> = FetcherArgumentsDefault(),
        cleanCache: Boolean = false,
        refreshCache: Boolean = false
    ) = intent {

        var cacheRefreshingStared = false

        // build a response info
        val requestId = nextRequestId()
        val info = ResponseInfo(requestId = requestId, arguments = arguments)

        // build a cache key
        val key = cacheKeyBuilder.build(arguments)

        // clean cache if required
        if (cleanCache) {
            cleanCache(arguments)
        } else {
            val cacheInfo = info.copy(origin = ResponseOrigin.Cache)

            if (checkIfTheSameRequestIsPending(
                    cacheRequests, requestId, key, cacheInfo, false
                )
            ) return@intent

            // get a value from the cache
            val value = try {
                coroutineScope {
                    async(Dispatchers.IO) {
                        cacheService?.get(key)
                    }.let {
                        cacheRequests.setAsync(requestId, key, it)
                        it.await()
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error on cache value getting with key=$key")
                null
            } finally {
                cacheRequests.clean(requestId, key)
            }
            value?.let {
                // response with the value from cache
                reduce { Response.Data(cacheInfo, it) }
                if (refreshCache) cacheRefreshingStared = true else return@intent
            }
        }

        val fetcherInfo = info.copy(origin = ResponseOrigin.Fetcher)

        if (!cacheRefreshingStared) {
            // response with the loading state
            reduce { Response.Loading(fetcherInfo) }
        }

        if (checkIfTheSameRequestIsPending(
                fetcherRequests, requestId, key, fetcherInfo, true
            )
        ) return@intent

        // request a value from the fetcher
        val value = try {
            coroutineScope {
                async(Dispatchers.IO) {
                    fetcherService.request(arguments)
                }.let {
                    fetcherRequests.setAsync(requestId, key, it)
                    it.await()
                }
            }
        } catch (e: Exception) {
            // cancellation of the request
            if (e is CancellationException) {
                reduce { state } // response with the current state
            } else {
                Timber.e(e, "Error on requesting to fetcher with args=$arguments")
                // convert the exception to ApiException and handle it by default
                val appException = errorHandler?.handle(e, errorHandlerSettings)
                    ?: AppException(cause = e)
                // then response with the error on data fetching
                reduce { Response.Error.Exception(appException, fetcherInfo) }
            }
            return@intent
        } finally {
            fetcherRequests.clean(requestId, key)
        }

        // cache the fetched value
        if (value != null && arguments.isCaching(value)) {
            try {
                cacheService?.set(key, value)
            } catch (e: Exception) {
                Timber.e(e, "Error on cache value setting with key=$key")
            }
        }

        // response with the value
        responseWithValue(value, fetcherInfo)
    }

    /**
     * Cleans the cache of the VM by the key built from the passed args
     */
    suspend fun cleanCache(arguments: FetcherArguments<T>) {
        withContext(Dispatchers.IO) {
            val key = cacheKeyBuilder.build(arguments)
            cleanCache(key)
            // for the case if we have a fetcher with an embedded cache
            try {
                fetcherService.evict(arguments)
            } catch (e: Exception) {
                Timber.e(e, "Error on cache cleaning with key=$arguments")
            }
        }
    }

    /**
     * @param value If false and in case of using data class of [FetcherArguments] implementation, then the VM
     * won't respond with the same states as it is conventional for [kotlinx.coroutines.flow.StateFlow]
     */
    fun withRequestId(value: Boolean) = apply { withRequestId.set(value) }

    private fun responseWithValue(value: T?, responseInfo: ResponseInfo) = intent {
        reduce {
            // if response is in the ignoring list
            if (fetcherRequests.isIgnoring(responseInfo.requestId))
                state // response with the current state
            else
                Response.Data(responseInfo, value)
        }
    }

    // If the same request was already started - responses with its result
    private suspend fun checkIfTheSameRequestIsPending(
        pendingRequests: PendingRequests<T>, requestId: Long, cacheKey: Any,
        responseInfo: ResponseInfo, valueIsNullable: Boolean
    ): Boolean {
        return pendingRequests.getAsync(requestId, cacheKey)?.let { deferred ->
            val value = deferred.await()
            if (valueIsNullable || value != null) {
                intent {
                    try {
                        responseWithValue(value, responseInfo)
                    } catch (e: Exception) {
                        reduce { state } // response with the current state
                    } finally {
                        pendingRequests.clean(requestId)
                    }
                }
                true
            } else false
        } ?: false
    }

    private suspend fun cleanCache(key: Any) {
        try {
            cacheService?.evict(key)
        } catch (e: Exception) {
            Timber.e(e, "Error on cache cleaning with key=$key")
        }
    }

    private fun nextRequestId(): Long {
        return if (withRequestId.get()) requestCounter.addAndGet(1) else 0
    }

    override fun onCleared() {
        super.onCleared()
        if (cancelPendingRequestsOnClearedVM) fetcherRequests.cancel()
    }

    // Some caches need "warming up" because the first request to cache takes much more time than following
    private fun cacheWarmUp() {
        cacheService ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    cacheService.get(0)
                } catch (_: Exception) {
                }
            }
        }
    }

    init {
        if (cacheWarmUpOnInit) cacheWarmUp()
    }
}

/**
 * Pending requests container
 */
private class PendingRequests<T : Any> {
    private val ignoringResponsesIds = ConcurrentHashMap<Long, Boolean>()
    private val pendingRequestsByIds = ConcurrentHashMap<Long, Deferred<T?>>()
    private val pendingRequestsByKeys = ConcurrentHashMap<Any, Deferred<T?>>()

    fun cancel() = apply {
        pendingRequestsByIds.values.forEach { it.cancel() }
        pendingRequestsByIds.clear()
        pendingRequestsByKeys.clear()
        ignoringResponsesIds.clear()
    }

    fun ignore() = apply {
        pendingRequestsByIds.keys.forEach { ignoringResponsesIds[it] = true }
    }

    fun getAsync(requestId: Long, key: Any): Deferred<T?>? {
        return pendingRequestsByKeys[key]?.apply {
            pendingRequestsByIds[requestId] = this
        }
    }

    fun setAsync(requestId: Long, key: Any, deferred: Deferred<T?>) {
        pendingRequestsByKeys[key] = deferred
        pendingRequestsByIds[requestId] = deferred
    }

    @Suppress("DeferredResultUnused")
    fun clean(requestId: Long, key: Any? = null) {
        pendingRequestsByIds.remove(requestId)
        key?.let { pendingRequestsByKeys.remove(key) }
    }

    fun isIgnoring(requestId: Long): Boolean {
        return ignoringResponsesIds.remove(requestId) == true
    }
}