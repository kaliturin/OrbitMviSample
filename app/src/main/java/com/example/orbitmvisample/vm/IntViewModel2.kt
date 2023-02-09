package com.example.orbitmvisample.vm

import com.example.orbitmvisample.fetcher.FetcherViewModel
import com.example.orbitmvisample.service.IntCatchingFetcherService

class IntViewModel2(
    fetcherService: IntCatchingFetcherService
) : FetcherViewModel<Int>(fetcherService)