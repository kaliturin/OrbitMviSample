package com.example.orbitmvisample.cache.impl

import com.appmattus.layercache.Cache
import com.appmattus.layercache.fromCache2k
import com.example.orbitmvisample.cache.CacheBuilder
import com.example.orbitmvisample.cache.CacheSettings
import org.cache2k.Cache2kBuilder

class CacheBuilderImpl : CacheBuilder {
    override fun build(settings: CacheSettings): Cache<Any, Any> {
        val builder = Cache2kBuilder.forUnknownTypes()
            .name(settings.cacheName)
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