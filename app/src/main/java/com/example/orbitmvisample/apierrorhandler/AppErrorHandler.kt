package com.example.orbitmvisample.apierrorhandler

import android.content.Context
import android.os.Bundle

interface AppErrorHandler {
    suspend fun handle(
        throwable: Throwable, context: Context? = null, settings: Bundle? = null
    ): AppException
}