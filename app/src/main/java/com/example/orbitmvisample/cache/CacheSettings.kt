package com.example.orbitmvisample.cache

import java.util.concurrent.TimeUnit

/**
 * Timed cache settings
 */
class CacheSettings(
    val cacheName: String = "default",
    val capacity: Int = 0,
    val timeToExpire: Long = 0,
    val timeUnit: TimeUnit = TimeUnit.SECONDS,
    val keepDataAfterExpired: Boolean = false,
    val eternal: Boolean = false,
    val cacheType: CacheType = CacheType.LRU
) {
    val timeToExpireMills = timeUnit.toMillis(timeToExpire)
}

enum class CacheType {
    LRU,
    CACHE2K,
    PREFERENCES
}