package com.example.orbitmvisample.cache.impl

import com.appmattus.layercache.Cache
import com.appmattus.layercache.createLruCache
import com.example.orbitmvisample.cache.CacheSettings

fun <K : Any, V : Any> Cache<String, String>.asTimedJsonCache(
    settings: CacheSettings
): Cache<K, V> = TimedCache(asJsonCache(), settings)

fun <K : Any, V : Any> Cache.Companion.createTimedLruCache(
    settings: CacheSettings
): Cache<K, V> {
    val lruCache = Cache.createLruCache<K, TimedCache.TimedValue<V>>(settings.capacity)
    return TimedCache(lruCache, settings)
}

private class TimedCache<K : Any, V : Any>(
    private val cache: Cache<K, TimedValue<V>>,
    private val settings: CacheSettings
) : Cache<K, V> {

    override suspend fun evict(key: K) {
        cache.evict(key)
    }

    override suspend fun evictAll() {
        cache.evictAll()
    }

    override suspend fun get(key: K): V? {
        return (cache.get(key))?.let { timed ->
            if (timed.time > currentTime()) {
                timed.value
            } else {
                if (!settings.keepDataAfterExpired) cache.evict(key)
                null
            }
        }
    }

    override suspend fun set(key: K, value: V) {
        cache.set(key, TimedValue(settings.timeToExpireMills + currentTime(), value))
    }

    private fun currentTime(): Long = System.currentTimeMillis()

    data class TimedValue<V>(
        val time: Long,
        val value: V
    )
}