package com.example.orbitmvisample.fetcher

import androidx.lifecycle.ViewModel
import com.appmattus.layercache.Cache
import com.example.orbitmvisample.apierrorhandler.ApiErrorHandler
import com.example.orbitmvisample.apierrorhandler.ApiException
import com.example.orbitmvisample.cache.CacheKeyBuilder
import com.example.orbitmvisample.cache.CacheKeyBuilderAny
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
open class FetcherViewModel<T : Any>(
    private val fetcherService: FetcherService<T>,
    private val errorHandler: ApiErrorHandler? = null,
    private val cacheService: Cache<Any, T>? = null,
    private var cacheKeyBuilder: CacheKeyBuilder? = null
) : ViewModel(), ContainerHost<Response<T>, Nothing> {

    override val container = container<Response<T>, Nothing>(Response.NoNewData())

    /**
     * If false and in case of using data class of [FetcherArguments] implementation, then the VM
     * won't respond with the same states as it is conventional for [kotlinx.coroutines.flow.StateFlow]
     */
    var withResponseId = AtomicBoolean(true)

    private var requestCounter = AtomicLong(0)

    /**
     * Sends request to VM to start for emitting [Response] states
     * @param arguments arguments of [FetcherService]
     * @param cleanCache if true - then removes a value from the cache before fetching it from [FetcherService]
     * @param refreshCache if true - then after response from cache VM trying to fetch a value from [FetcherService]
     */
    fun request(
        arguments: FetcherArguments<T> = FetcherArgumentsDefault(),
        cleanCache: Boolean = false,
        refreshCache: Boolean = false,
        withResponseId: Boolean = this.withResponseId.get()
    ) = intent {

        var refreshCacheStared = false

        // build response info
        val responseId = if (withResponseId) requestCounter.addAndGet(1) else 0
        val info = ResponseInfo(responseId = responseId, arguments = arguments)

        // build a cache key
        val key = cacheKeyBuilder?.build(arguments)

        if (key != null) {
            // clean cache if required
            if (cleanCache) {
                cleanCache(key)
            } else {
                // get a value from the cache
                val value = try {
                    cacheService?.get(key)
                } catch (e: Exception) {
                    Timber.e(e, "Error on cache value getting with key=$key")
                    null
                }
                value?.let {
                    // state of response with data from the cache
                    reduce { Response.Data(info.copy(origin = ResponseOrigin.Cache), it) }
                    if (refreshCache) refreshCacheStared = true else return@intent
                }
            }
        }

        val isAlreadyLoading = state is Response.Loading

        if (!refreshCacheStared) {
            // state of response about loading is started
            reduce { Response.Loading(info.copy(origin = ResponseOrigin.Fetcher)) }
        }

        // if loading was already started - don't fetch again
        if (isAlreadyLoading) return@intent

        // request a value from fetcher
        val value = try {
            withContext(Dispatchers.IO) {
                fetcherService.request(arguments)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error on requesting to fetcher with args=$arguments")
            // convert the exception to ApiException and handle it by default
            val apiException = errorHandler?.handle(e) ?: ApiException(cause = e)
            // then state of response with the error on data fetching
            reduce {
                Response.Error.Exception(
                    apiException, info.copy(origin = ResponseOrigin.Fetcher)
                )
            }
            return@intent
        }

        if (cacheService != null && key != null && value != null && arguments.isCaching(value)) {
            try {
                // put the value to the cache
                cacheService.set(key, value)
            } catch (e: Exception) {
                Timber.e(e, "Error on cache value setting with key=$key")
            }
        }

        // state of response with data from the fetcher
        reduce {
            if (value != null)
                Response.Data(info.copy(origin = ResponseOrigin.Fetcher), value)
            else
                Response.NoNewData(info.copy(origin = ResponseOrigin.Fetcher))
        }
    }

    suspend fun cleanCache(arguments: FetcherArguments<T>) {
        withContext(Dispatchers.Default) {
            try {
                fetcherService.evict(arguments)
            } catch (e: Exception) {
                Timber.e(e, "Error on cache cleaning with key=$arguments")
            }
            cacheKeyBuilder?.build(arguments)?.let { cleanCache(it) }
        }
    }

    private suspend fun cleanCache(key: Any) {
        try {
            cacheService?.evict(key)
        } catch (e: Exception) {
            Timber.e(e, "Error on cache cleaning with key=$key")
        }
    }

    init {
        if (cacheService != null && cacheKeyBuilder == null) {
            // build default cache key builder
            cacheKeyBuilder = CacheKeyBuilderAny(fetcherService::class.qualifiedName)
        }
    }
}