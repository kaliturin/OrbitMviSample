package com.example.orbitmvisample.fetcher

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModel
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
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * VM with data fetching service and optional cache service
 */
@Suppress("unused")
open class OrbitFetcherViewModel<T : Any>(
    private val fetcherService: FetcherService<T>,
    private val errorHandler: AppErrorHandler? = null,
    private val cacheService: Cache<Any, T>? = null,
    private var cacheKeyBuilder: CacheKeyBuilder = CacheKeyBuilderDefault(fetcherService::class.qualifiedName)
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
    @Synchronized
    fun cancelPendingRequests() = apply {
        cacheRequests.cancel()
        fetcherRequests.cancel()
    }

    /**
     * Allows to ignore the responses of the current pending requests but cache its results
     */
    @Synchronized
    fun ignorePendingRequests() = apply {
        cacheRequests.ignore()
        fetcherRequests.ignore()
    }

    /**
     * @param value If false and in case of using data class of [FetcherArguments] implementation, then the VM
     * won't respond with the same states as it is conventional for [kotlinx.coroutines.flow.StateFlow]
     */
    @SuppressWarnings("WeakerAccess")
    fun withRequestId(value: Boolean = true) = apply { withRequestId.set(value) }

    /**
     * @see [withRequestId]
     */
    fun withoutRequestId() = withRequestId(false)

    fun cancelPendingRequestsOnClearVM(value: Boolean) = apply {
        cancelPendingRequestsOnClearedVM = value
    }

    /**
     * Requests the VM to start emitting [Response] states
     * @param arguments arguments of [FetcherService]
     * @param context may be used in error handler component to show some default alerts
     * @param cleanCache if true - then removes a value from the cache before fetching it from [FetcherService]
     * @param refreshCache if true - then after response from cache VM trying to fetch a value from [FetcherService]
     */
    @Synchronized
    fun request(
        arguments: FetcherArguments<T> = FetcherArgumentsDefault(),
        context: Context? = null,
        cleanCache: Boolean = false,
        refreshCache: Boolean = false
    ) = intent {

        var cacheRefreshingStared = false

        // build a response info
        val requestId = nextRequestId()
        val responseInfo = ResponseInfo(requestId = requestId, arguments = arguments)

        // build a cache key
        val key = cacheKeyBuilder.build(arguments)

        // clean cache if required
        if (cleanCache) {
            cleanCache(arguments)
        } else {
            val cacheInfo = responseInfo.copy(origin = ResponseOrigin.Cache)

            if (checkIfTheSameRequestIsPending(
                    cacheInfo, cacheRequests, requestId, key, false
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
                // cancellation of the request
                if (e is CancellationException) {
                    reduce { Response.Cancelled(cacheInfo) }
                    return@intent
                } else {
                    Timber.e(e, "Error on cache value getting with key=$key")
                    null
                }
            } finally {
                cacheRequests.clean(requestId, key)
            }
            value?.let {
                // response with the value from cache
                reduce { Response.Data(cacheInfo, it) }
                if (refreshCache) cacheRefreshingStared = true else return@intent
            }
        }

        val fetcherInfo = responseInfo.copy(origin = ResponseOrigin.Fetcher)

        if (!cacheRefreshingStared) {
            reduce { Response.Loading(fetcherInfo) } // response with the loading state
        }

        if (checkIfTheSameRequestIsPending(
                fetcherInfo, fetcherRequests, requestId, key, true
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
                reduce { Response.Cancelled(fetcherInfo) }
            } else {
                Timber.e(e, "Error on requesting to fetcher with args=$arguments")
                // convert the exception to ApiException and handle it by default
                val appException = errorHandler?.handle(e, context, errorHandlerSettings)
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
        responseWithValue(fetcherInfo, fetcherRequests, value)
    }

    /**
     * Cleans the cache of the VM by the key built from the passed args
     */
    @SuppressWarnings("WeakerAccess")
    suspend fun cleanCache(arguments: FetcherArguments<T>) {
        withContext(Dispatchers.IO) {
            val key = cacheKeyBuilder.build(arguments)
            cleanCache(key)
            try {
                // for the case if we have a fetcher with an embedded cache
                fetcherService.evict(arguments)
            } catch (e: Exception) {
                Timber.e(e, "Error on cache cleaning with key=$arguments")
            }
        }
    }

    private fun responseWithValue(
        responseInfo: ResponseInfo, pendingRequests: PendingRequests<T>, value: T?
    ) = intent {
        reduce {
            if (pendingRequests.isIgnored(responseInfo.requestId)) // if request id is in the ignored list
                state // response with the current state
            else
                Response.Data(responseInfo, value)
        }
    }

    // If the same request was already started - responses with its result
    private suspend fun checkIfTheSameRequestIsPending(
        responseInfo: ResponseInfo, pendingRequests: PendingRequests<T>,
        requestId: Long, cacheKey: Any, valueIsNullable: Boolean
    ): Boolean {
        return pendingRequests.getAsync(requestId, cacheKey)?.let { deferred ->
            val value = deferred.await()
            val result = if (valueIsNullable || value != null) {
                responseWithValue(responseInfo, pendingRequests, value)
                true
            } else false
            pendingRequests.clean(requestId)
            result
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
        if (cancelPendingRequestsOnClearedVM) cancelPendingRequests()
    }

    /**
     * Pending requests container
     */
    private class PendingRequests<T : Any> {
        private val ignoredResponsesIds = mutableMapOf<Long, Boolean>()
        private val pendingRequestsByIds = mutableMapOf<Long, Deferred<T?>>()
        private val pendingRequestsByKeys = mutableMapOf<Any, Deferred<T?>>()

        @Synchronized
        fun cancel() = apply {
            pendingRequestsByIds.values.forEach { it.cancel() }
            pendingRequestsByIds.clear()
            pendingRequestsByKeys.clear()
            ignoredResponsesIds.clear()
        }

        @Synchronized
        fun ignore() = apply {
            pendingRequestsByIds.keys.forEach { ignoredResponsesIds[it] = true }
        }

        @Synchronized
        fun getAsync(requestId: Long, key: Any): Deferred<T?>? {
            return pendingRequestsByKeys[key]?.apply {
                pendingRequestsByIds[requestId] = this
            }
        }

        @Synchronized
        fun setAsync(requestId: Long, key: Any, deferred: Deferred<T?>) {
            pendingRequestsByKeys[key] = deferred
            pendingRequestsByIds[requestId] = deferred
        }

        @Suppress("DeferredResultUnused")
        @Synchronized
        fun clean(requestId: Long, key: Any? = null) {
            pendingRequestsByIds.remove(requestId)
            key?.let { pendingRequestsByKeys.remove(key) }
        }

        @Synchronized
        fun isIgnored(requestId: Long): Boolean {
            return ignoredResponsesIds.remove(requestId) == true
        }
    }
}