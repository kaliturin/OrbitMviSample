package com.example.orbitmvisample.apierrorhandler

import android.os.Bundle

interface AppErrorHandler {
    suspend fun handle(throwable: Throwable, settings: Bundle? = null): AppException
}