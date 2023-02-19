package com.example.orbitmvisample.cache

import java.util.concurrent.TimeUnit

/**
 * Timed cache settings
 */
class CacheSettings(
    val cacheName: String = "",
    val capacity: Long? = null,
    val timeToExpire: Long? = null,
    val timeUnit: TimeUnit = TimeUnit.SECONDS,
    val keepDataAfterExpired: Boolean = false,
    val eternal: Boolean = false
)