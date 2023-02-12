package com.example.orbitmvisample.apierrorhandler.impl

import com.example.orbitmvisample.apierrorhandler.ApiErrorHandler
import com.example.orbitmvisample.apierrorhandler.ApiException
import com.example.orbitmvisample.apierrorhandler.ApiExceptionBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Handles an exception as an [ApiException]
 * @param builder builds the [ApiException]
 * @param handlers upper level handlers that receives the already built [ApiException]
 */
class ApiErrorHandlerImpl(
    private val builder: ApiExceptionBuilder,
    private vararg val handlers: ApiErrorHandler
) : ApiErrorHandler {

    override suspend fun handle(throwable: Throwable): ApiException {
        val exception = withContext(Dispatchers.Default) {
            builder.build(throwable)
        }
        Timber.e(exception.toString())
        handlers.forEach { it.handle(exception) }
        return exception
    }
}