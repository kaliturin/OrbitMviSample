package com.example.orbitmvisample.cache

import java.util.concurrent.TimeUnit

/**
 * Timed cache settings
 */
class CacheSettings(
    val cacheName: String,          // unique cache name
    val capacity: Int = 100,        // max capacity in number of entries (if supports)
    val size: Long = 10240,         // max size of persistent cache in bytes (if supports)
    val timeToExpire: Long = 0,
    val timeUnit: TimeUnit = TimeUnit.SECONDS,
    val keepDataAfterExpired: Boolean = false,
    val eternal: Boolean = false,
    val cacheType: CacheType = CacheType.LRU
) {
    val timeToExpireMills = timeUnit.toMillis(timeToExpire)
}

/**
 * Available cache types
 */
enum class CacheType {
    LRU,        // in memory LruCache
    CACHE2K,    // in memory Cache2K
    DISK_LRU,   // J.Wharton's DiskLruCache
    PREFERENCES // DataStore Preferences disk cache (doesn't support size and capacity restrictions yet)
}