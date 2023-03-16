package com.example.orbitmvisample.websocket.impl

import com.example.orbitmvisample.apierrorhandler.AppErrorHandler
import com.example.orbitmvisample.apierrorhandler.AppException
import com.example.orbitmvisample.apierrorhandler.impl.AppErrorHandlerDispatcher
import com.example.orbitmvisample.websocket.WebSocketFlow
import timber.log.Timber

/**
 * An example of simple websocket's response deserializer that actually deserializes just
 * only a throwable
 */
class StringResponseDeserializer(
    private val errorHandler: AppErrorHandler? = AppErrorHandlerDispatcher()
) : WebSocketFlow.ResponseDeserializer<String> {

    override suspend fun onThrowable(throwable: Throwable): String {
        val appException = errorHandler?.handle(throwable) ?: AppException(cause = throwable)
        Timber.e(appException)
        return appException.toString()
    }

    override suspend fun fromString(string: String): String? {
        if (isIgnoring(string)) return null
        return string
    }

    // Returns true if ignores a response because of the content
    private fun isIgnoring(string: String): Boolean {
        return string == TEXT_OPEN
    }

    companion object {
        const val TEXT_OPEN = "OPEN"
    }
}