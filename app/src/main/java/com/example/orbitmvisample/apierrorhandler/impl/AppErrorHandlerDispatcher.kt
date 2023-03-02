package com.example.orbitmvisample.apierrorhandler.impl

import android.os.Bundle
import com.example.orbitmvisample.apierrorhandler.AppErrorHandler
import com.example.orbitmvisample.apierrorhandler.AppException
import com.example.orbitmvisample.apierrorhandler.AppExceptionBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Handles an exception as an [AppException]
 * @param builder builds the [AppException]
 * @param handlers upper level handlers that receives the already built [AppException]
 */
class AppErrorHandlerDispatcher(
    private val builder: AppExceptionBuilder,
    private vararg val handlers: AppErrorHandler
) : AppErrorHandler {

    override suspend fun handle(throwable: Throwable, settings: Bundle?): AppException {
        val exception = withContext(Dispatchers.Default) {
            builder.build(throwable)
        }
        Timber.e(exception.toString())
        handlers.forEach { it.handle(exception, settings) }
        return exception
    }
}