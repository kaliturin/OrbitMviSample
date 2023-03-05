package com.example.orbitmvisample.cache.impl

import com.appmattus.layercache.Cache
import com.appmattus.layercache.fromCache2k
import com.example.orbitmvisample.cache.CacheBuilder
import com.example.orbitmvisample.cache.CacheSettings
import org.cache2k.Cache2kBuilder
import kotlin.reflect.KClass

/**
 * Builds [com.appmattus.layercache.Cache] cache that is wrapping [org.cache2k.Cache] cache
 */
class Cache2KBuilder : CacheBuilder {
    override fun <K : Any, V : Any> build(settings: CacheSettings, clazz: KClass<V>): Cache<K, V>? {
        val builder = Cache2kBuilder.forUnknownTypes()
            .name(settings.cacheName)
            .keepDataAfterExpired(settings.keepDataAfterExpired)
        if (settings.eternal || settings.timeToExpire <= 0L) {
            builder.eternal(true)
        } else {
            builder.expireAfterWrite(settings.timeToExpire, settings.timeUnit)
        }
        if (settings.capacity != 0) {
            builder.entryCapacity(settings.capacity.toLong())
        }
        @Suppress("unchecked_cast")
        return (builder.build() as? org.cache2k.Cache<K, V>)?.let {
            Cache.fromCache2k(it).reuseInflight()
        }
    }
}