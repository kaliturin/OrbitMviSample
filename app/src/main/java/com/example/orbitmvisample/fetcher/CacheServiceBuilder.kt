package com.example.orbitmvisample.fetcher

import com.appmattus.layercache.Cache
import com.appmattus.layercache.fromCache2k
import org.cache2k.Cache2kBuilder

class CacheServiceBuilder(private val settings: CacheSettings) {
    fun build(): Cache<Any, Any> {
        val builder = Cache2kBuilder.forUnknownTypes()
            .name(settings.name)
            .keepDataAfterExpired(settings.keepDataAfterExpired)
        if (settings.eternal || settings.timeToExpire == null) {
            builder.eternal(true)
        } else {
            builder.expireAfterWrite(settings.timeToExpire, settings.timeUnit)
        }
        if (settings.capacity != null) {
            builder.entryCapacity(settings.capacity)
        }
        return Cache.fromCache2k(builder.build()).reuseInflight()
    }
}