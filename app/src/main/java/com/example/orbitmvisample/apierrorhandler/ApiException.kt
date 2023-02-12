package com.example.orbitmvisample.apierrorhandler

class ApiException(
    val errorCode: ApiErrorCode,
    message: String?,
    cause: Throwable? = null
) : RuntimeException(message, cause) {

    constructor(message: String? = null, cause: Throwable? = null) :
            this(ApiErrorCode.UNKNOWN, message, cause)

    override fun toString(): String {
        return "ApiException errorCode = $errorCode, ${super.toString()}"
    }
}