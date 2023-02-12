package com.example.orbitmvisample.vm

import com.example.orbitmvisample.apierrorhandler.ApiErrorHandler
import com.example.orbitmvisample.fetcher.FetcherViewModel
import com.example.orbitmvisample.service.IntCatchingFetcherService

class IntViewModel2(
    fetcherService: IntCatchingFetcherService,
    errorHandler: ApiErrorHandler
) : FetcherViewModel<Int>(fetcherService, errorHandler)