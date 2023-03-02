package com.example.orbitmvisample.cache.impl

import com.example.orbitmvisample.cache.CacheManager.Companion.DEFAULT_CACHE_NAME
import com.example.orbitmvisample.cache.CacheSettings
import com.example.orbitmvisample.cache.CacheType

/**
 * Timed cache names
 */
const val CACHE_DEFAULT = DEFAULT_CACHE_NAME
const val CACHE_3_SEC = "CACHE_3_SEC"
const val CACHE_10_SEC = "CACHE_10_SEC"
const val CACHE_15_SEC = "CACHE_15_SEC"
const val CACHE_30_SEC = "CACHE_30_SEC"
const val CACHE_1_MIN = "CACHE_1_MIN"
const val CACHE_5_MIN = "CACHE_5_MIN"
const val CACHE_ETERNAL = "CACHE_ETERNAL"
const val PREFS_ETERNAL = "PREFS_ETERNAL"

/**
 * List of predefined cache settings
 */
val defaultListOfCacheSettings = listOf(
    CacheSettings(
        cacheName = CACHE_DEFAULT,
        capacity = 1000,
        eternal = true,
        cacheType = CacheType.CACHE2K
    ),
    CacheSettings(cacheName = CACHE_3_SEC, capacity = 50, timeToExpire = 3),
    CacheSettings(cacheName = CACHE_10_SEC, capacity = 50, timeToExpire = 10),
    CacheSettings(
        cacheName = CACHE_15_SEC,
        capacity = 100,
        timeToExpire = 15,
        cacheType = CacheType.PREFERENCES
    ),
    CacheSettings(cacheName = CACHE_30_SEC, capacity = 100, timeToExpire = 30),
    CacheSettings(cacheName = CACHE_1_MIN, capacity = 200, timeToExpire = 60),
    CacheSettings(cacheName = CACHE_5_MIN, capacity = 300, timeToExpire = 300),
    CacheSettings(cacheName = CACHE_ETERNAL, capacity = 2000, eternal = true),
    CacheSettings(cacheName = PREFS_ETERNAL, eternal = true, cacheType = CacheType.PREFERENCES),
)