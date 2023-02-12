package com.example.orbitmvisample.apierrorhandler

interface ApiErrorHandler {
    suspend fun handle(throwable: Throwable): ApiException
}