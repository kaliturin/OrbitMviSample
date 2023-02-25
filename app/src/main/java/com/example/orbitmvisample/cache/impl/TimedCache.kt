package com.example.orbitmvisample.cache.impl

import com.appmattus.layercache.Cache
import com.example.orbitmvisample.cache.CacheSettings

fun <K : Any, V : Any> Cache<String, String>.asTimedJsonCache(
    settings: CacheSettings
): Cache<K, V> = TimedCache(asJsonCache(), settings)

private class TimedCache<K : Any, V : Any>(
    private val cache: Cache<Any, TimedValue<V>>,
    private val settings: CacheSettings
) : Cache<K, V> {

    private val timeToExpire = settings.timeUnit.toMillis(settings.timeToExpire ?: 0)

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
        cache.set(key, TimedValue(timeToExpire + currentTime(), value))
    }

    private fun currentTime(): Long = System.currentTimeMillis()

    data class TimedValue<V>(
        val time: Long,
        val value: V
    )
}
