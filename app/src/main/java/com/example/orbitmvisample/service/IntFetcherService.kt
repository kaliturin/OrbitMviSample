package com.example.orbitmvisample.service

import com.example.orbitmvisample.fetcher.FetcherArguments
import com.example.orbitmvisample.fetcher.FetcherService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import timber.log.Timber
import kotlin.random.Random

class IntFetcherService : FetcherService<Int> {
    override suspend fun request(arguments: FetcherArguments?): Int {
        val args = arguments as? Arguments ?: Arguments()
        Timber.d("request data called in thread: " + Thread.currentThread().toString())
        return coroutineScope {
            val asyncResult = async(Dispatchers.IO) {
                Timber.d("fetching data in thread: " + Thread.currentThread().toString())
                delay(2000)
                Random.nextInt() % args.param
            }
            asyncResult.await()
        }
    }

    data class Arguments(val param: Int = 10) : FetcherArguments {
        override fun getCacheKey(): Any = param
    }
}