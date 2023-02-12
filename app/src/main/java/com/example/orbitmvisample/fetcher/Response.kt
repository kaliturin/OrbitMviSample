package com.example.orbitmvisample.fetcher

import com.example.orbitmvisample.apierrorhandler.ApiException

/**
 * Holder for responses from [FetcherViewModel]
 */
sealed class Response<out T> {

    /**
     * Contains additional response information
     */
    abstract val info: ResponseInfo

    /**
     * Loading event dispatched by [FetcherViewModel] to signal the [FetcherService] is in progress.
     */
    data class Loading(
        override val info: ResponseInfo
    ) : Response<Nothing>()

    /**
     * Data dispatched by [FetcherViewModel]
     */
    data class Data<T>(
        override val info: ResponseInfo,
        val value: T,
    ) : Response<T>()

    /**
     * No new data event dispatched by [FetcherViewModel] to signal the [FetcherService] returned no data (i.e the
     * returned [kotlinx.coroutines.flow.Flow], when collected, was empty).
     */
    data class NoNewData(
        override val info: ResponseInfo = ResponseInfo(),
    ) : Response<Nothing>()

    /**
     * Error dispatched by a pipeline
     */
    sealed class Error : Response<Nothing>() {
        data class Exception(
            val error: ApiException,
            override val info: ResponseInfo,
        ) : Error()

        data class Message(
            val message: String,
            override val info: ResponseInfo
        ) : Error()
    }

    /**
     * Returns the available data or throws [NullPointerException] if there is no data.
     */
    fun requireData(): T {
        return when (this) {
            is Data -> value
            is Error -> this.doThrow()
            else -> throw NullPointerException("there is no data in $this")
        }
    }

    /**
     * If this [Response] is of type [Response.Error], throws the exception
     * Otherwise, does nothing.
     */
    fun throwIfError() {
        if (this is Error) {
            this.doThrow()
        }
    }

    /**
     * If this [Response] is of type [Response.Error], returns the available error
     * from it. Otherwise, returns `null`.
     */
    fun errorMessageOrNull(): String? {
        return when (this) {
            is Error.Message -> message
            is Error.Exception -> error.message ?: "exception: ${error::class}"
            else -> null
        }
    }

    /**
     * If there is data available, returns it; otherwise returns null.
     */
    fun dataOrNull(): T? = when (this) {
        is Data -> value
        else -> null
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <R> swapType(): Response<R> = when (this) {
        is Error -> this
        is Loading -> this
        is NoNewData -> this
        is Data -> throw RuntimeException("cannot swap type for StoreResponse.Data")
    }
}

/**
 * Represents the origin for a [Response].
 */
enum class ResponseOrigin {
    /**
     * [Response] is sent from the cache
     */
    Cache,

    /**
     * [Response] is sent from a fetcher,
     */
    Fetcher,

    Undefined
}

fun Response.Error.doThrow(): Nothing = when (this) {
    is Response.Error.Exception -> throw error
    is Response.Error.Message -> throw RuntimeException(message)
}

data class ResponseInfo(
    /**
     * Represents the source of the Response.
     */
    val origin: ResponseOrigin = ResponseOrigin.Undefined,
    val responseId: Long = 0,
    val arguments: FetcherArguments<*>? = null
) {
    @Suppress("UNCHECKED_CAST")
    fun <T> getArguments(): T? {
        return arguments as? T
    }
}
