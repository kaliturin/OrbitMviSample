package com.example.orbitmvisample.apierrorhandler

interface AppExceptionBuilder {
    suspend fun build(throwable: Throwable): AppException
}