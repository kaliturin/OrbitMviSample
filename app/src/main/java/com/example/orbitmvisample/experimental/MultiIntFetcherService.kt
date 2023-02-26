package com.example.orbitmvisample.experimental

import com.example.orbitmvisample.fetcher.FetcherArguments
import com.example.orbitmvisample.fetcher.FetcherService
import com.example.orbitmvisample.service.IntFetcherService

class MultiIntFetcherService(
    private val service1: FetcherService<Int>,
    private val service2: FetcherService<Int>,
) : FetcherService<MultiIntFetcherService.Response> {

    override suspend fun request(arguments: FetcherArguments<Response>): Response {
        val args = arguments as Arguments

        val args1 = IntFetcherService.Arguments(args.param)
        val value1 = service1.get(args1)

        val args2 = IntFetcherService.Arguments(args.param)
        val value2 = service2.get(args2)

        return Response(value1, value2)
    }

    data class Arguments(val param: Int = 10) : FetcherArguments<Response> {
        override fun getCacheKey(): Any = param
    }

    class Response(val value1: Int?, val value2: Int?)
}