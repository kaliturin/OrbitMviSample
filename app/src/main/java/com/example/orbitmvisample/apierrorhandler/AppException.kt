package com.example.orbitmvisample.apierrorhandler

class AppException(
    val errorCode: AppErrorCode,
    message: String?,
    cause: Throwable? = null
) : RuntimeException(message, cause) {

    constructor(message: String? = null, cause: Throwable? = null) :
            this(AppErrorCode.UNKNOWN, message, cause)

    override fun toString(): String {
        return "AppException errorCode = $errorCode, ${super.toString()}"
    }
}