package com.example.orbitmvisample.fetcher

import java.util.concurrent.TimeUnit

/**
 * Timed cache settings
 */
class CacheSettings(
    val name: String = "",
    val capacity: Long? = null,
    val timeToExpire: Long? = null,
    val timeUnit: TimeUnit = TimeUnit.SECONDS,
    val keepDataAfterExpired: Boolean = false,
    val eternal: Boolean = false
)