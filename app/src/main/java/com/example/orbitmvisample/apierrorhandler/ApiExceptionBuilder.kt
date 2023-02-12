package com.example.orbitmvisample.apierrorhandler

interface ApiExceptionBuilder {
    suspend fun build(throwable: Throwable): ApiException
}