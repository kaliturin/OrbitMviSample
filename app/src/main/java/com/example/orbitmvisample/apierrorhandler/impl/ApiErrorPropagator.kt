package com.example.orbitmvisample.apierrorhandler.impl

import com.example.orbitmvisample.apierrorhandler.ApiErrorCode
import com.example.orbitmvisample.apierrorhandler.ApiErrorHandler
import com.example.orbitmvisample.apierrorhandler.ApiException

class ApiErrorPropagator : ApiErrorHandler {
    override suspend fun handle(throwable: Throwable): ApiException {
        val exception = throwable as? ApiException
            ?: throw IllegalArgumentException(
                "${ApiException::class.qualifiedName}" +
                        " as argument is expecting"
            )
        when (exception.errorCode) {
            ApiErrorCode.UNKNOWN -> {
                // TODO: track exception
            }
            ApiErrorCode.USER_IS_NOT_AUTHORIZED,
            ApiErrorCode.SESSION_CLOSED,
            ApiErrorCode.TECHNICAL_WORKS -> {
                // TODO: propagate exception
            }
            else -> {
            }
        }
        return exception
    }
}