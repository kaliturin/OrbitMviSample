package com.example.orbitmvisample.experimental

import com.example.orbitmvisample.apierrorhandler.ApiErrorHandler
import com.example.orbitmvisample.fetcher.FetcherViewModel

class IntViewModelWithCatchingService(
    fetcherService: IntCatchingService,
    errorHandler: ApiErrorHandler
) : FetcherViewModel<Int>(fetcherService, errorHandler = errorHandler)