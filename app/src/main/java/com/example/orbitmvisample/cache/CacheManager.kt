package com.example.orbitmvisample.cache

import com.appmattus.layercache.Cache
import java.util.concurrent.ConcurrentHashMap

class CacheManager(private val builder: CacheBuilder) {
    private val cachesMap = ConcurrentHashMap<String, Cache<Any, Any>>()
    private val settingsMap = ConcurrentHashMap<String, CacheSettings>()
    private val defSettings =
        CacheSettings(
            cacheName = DEFAULT_CACHE,
            timeToExpire = DEFAULT_TIME_TO_EXPIRE,
            capacity = DEFAULT_CAPACITY
        )

    fun addSettings(vararg settings: CacheSettings) = apply {
        settings.forEach {
            settingsMap[it.cacheName] = it
        }
    }

    fun getCache(cacheName: String): Cache<Any, Any> {
        var cache = cachesMap[cacheName]
        if (cache == null) {
            val settings = getSettingsOrDefault(cacheName)
            cache = cachesMap[settings.cacheName]
            if (cache == null) {
                cache = builder.build(settings)
                cachesMap[cacheName] = cache
            }
        }
        return cache
    }

    suspend fun cleanCache(cacheName: String) {
        cachesMap[cacheName]?.evictAll()
    }

    suspend fun cleanCache() {
        cachesMap.values.forEach { it.evictAll() }
    }

    private fun getSettingsOrDefault(cacheName: String): CacheSettings {
        return settingsMap[cacheName] ?: defSettings
    }

    companion object {
        private const val DEFAULT_CACHE = "DEFAULT_CACHE_38e43d05t8y0"
        private const val DEFAULT_TIME_TO_EXPIRE = 300L
        private const val DEFAULT_CAPACITY = 1000L
    }
}