package com.example.orbitmvisample.apierrorhandler.impl

import android.content.Context
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
    private val builder: AppExceptionBuilder = AppExceptionBuilderImpl(),
    private vararg val handlers: AppErrorHandler
) : AppErrorHandler {

    override suspend fun handle(
        throwable: Throwable, context: Context?, settings: Bundle?
    ): AppException {
        val exception = withContext(Dispatchers.Default) {
            builder.build(throwable)
        }
        Timber.e(exception.toString())
        handlers.forEach {
            try {
                it.handle(exception, context, settings)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        return exception
    }
}