package com.example.orbitmvisample.fetcher

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
    private var cacheKeyBuilder: CacheKeyBuilder = CacheKeyBuilderDefault(fetcherService::class.qualifiedName)
) : ViewModel(), ContainerHost<Response<T>, Nothing> {

    override val container = container<Response<T>, Nothing>(Response.NoNewData())

    private val withRequestId = AtomicBoolean(true)
    private val requestCounter = AtomicLong(0)
    private val ignoringResponsesIds = ConcurrentHashMap<Long, Boolean>()
    private val pendingRequestsByIds = ConcurrentHashMap<Long, Deferred<T?>>()
    private val pendingRequestsByKeys = ConcurrentHashMap<Any, Deferred<T?>>()
    private var errorHandlerSettings: Bundle? = null

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
        pendingRequestsByIds.values.forEach { it.cancel() }
        pendingRequestsByIds.clear()
        pendingRequestsByKeys.clear()
    }

    /**
     * Allows to ignore the responses of the current pending requests but cache its results
     */
    fun ignorePendingRequests() = apply {
        pendingRequestsByIds.keys.forEach { ignoringResponsesIds[it] = true }
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

        var refreshCacheStared = false

        // build a response info
        val requestId = nextRequestId()
        val info = ResponseInfo(requestId = requestId, arguments = arguments)

        // build a cache key
        val key = cacheKeyBuilder.build(arguments)

        // clean cache if required
        if (cleanCache) {
            cleanCache(arguments)
        } else {
            // get a value from the cache
            val value = try {
                cacheService?.get(key)
            } catch (e: Exception) {
                Timber.e(e, "Error on cache value getting with key=$key")
                null
            }
            value?.let {
                // response with data from the cache
                reduce { Response.Data(info.copy(origin = ResponseOrigin.Cache), it) }
                if (refreshCache) refreshCacheStared = true else return@intent
            }
        }

        val fetcherInfo = info.copy(origin = ResponseOrigin.Fetcher)

        if (!refreshCacheStared) {
            // response about loading is started
            reduce { Response.Loading(fetcherInfo) }
        }

        // if the same request was already started - await its result
        pendingRequestsByKeys[key]?.let { deferred ->
            pendingRequestsByIds[requestId] = deferred
            val value = try {
                deferred.await().also { cleanUp(requestId) }
            } catch (_: Exception) {
                reduce { state } // response with the current state
                cleanUp(requestId)
                return@intent
            }
            responseWithValue(value, fetcherInfo)
            return@intent
        }

        // request a value from the fetcher
        val value = try {
            coroutineScope {
                async(Dispatchers.IO) {
                    fetcherService.request(arguments)
                }.let { deferred ->
                    pendingRequestsByKeys[key] = deferred
                    pendingRequestsByIds[requestId] = deferred
                    deferred.await().also { cleanUp(requestId, key) }
                }
            }
        } catch (e: Exception) {
            // cancellation of the request
            if (e is CancellationException) {
                cleanUp(requestId, key)
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
        }

        if (value != null && arguments.isCaching(value)) {
            try {
                // put the value to the cache
                cacheService?.set(key, value)
            } catch (e: Exception) {
                Timber.e(e, "Error on cache value setting with key=$key")
            }
        }

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

    private fun responseWithValue(value: T?, info: ResponseInfo) = intent {
        reduce {
            // if response is in the ignoring list
            if (ignoringResponsesIds.remove(info.requestId) == true) {
                state // response with the current state
            } else {
                Response.Data(info, value)
            }
        }
    }

    private suspend fun cleanCache(key: Any) {
        try {
            cacheService?.evict(key)
        } catch (e: Exception) {
            Timber.e(e, "Error on cache cleaning with key=$key")
        }
    }

    @Suppress("DeferredResultUnused")
    private fun cleanUp(requestId: Long, key: Any? = null) {
        pendingRequestsByIds.remove(requestId)
        key?.let { pendingRequestsByKeys.remove(key) }
    }

    private fun nextRequestId(): Long {
        return if (withRequestId.get()) requestCounter.addAndGet(1) else 0
    }

    override fun onCleared() {
        super.onCleared()
        ignoringResponsesIds.clear()
        pendingRequestsByIds.clear()
        pendingRequestsByKeys.clear()
        errorHandlerSettings = null
    }
}