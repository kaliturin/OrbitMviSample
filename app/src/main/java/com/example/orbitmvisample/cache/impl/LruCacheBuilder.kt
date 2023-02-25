package com.example.orbitmvisample.cache.impl

import com.appmattus.layercache.Cache
import com.appmattus.layercache.createLruCache
import com.example.orbitmvisample.cache.CacheBuilder
import com.example.orbitmvisample.cache.CacheSettings
import kotlin.reflect.KClass

class LruCacheBuilder : CacheBuilder {
    override fun <K : Any, V : Any> build(settings: CacheSettings, clazz: KClass<V>): Cache<K, V> {
        val size = settings.capacity.let { if (it > 0) it else 1 }
        val cache = Cache.createLruCache<K, V>(size)
        return if (settings.eternal || settings.timeToExpire <= 0) cache
        else cache.asTimedCache(settings)
    }
}