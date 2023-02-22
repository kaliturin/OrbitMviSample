package com.example.orbitmvisample.cache

import com.appmattus.layercache.Cache
import java.util.concurrent.ConcurrentHashMap

/**
 * Cache manager allows to obtain [com.appmattus.layercache.Cache]<K, V> cache,
 * that is built by passed cache builder and passed settings.
 */
class CacheManager(
    private val builder: CacheBuilder,
    vararg settings: CacheSettings = defSettings
) {
    private val cachesMap = ConcurrentHashMap<String, Cache<*, *>>()
    private val settingsMap = ConcurrentHashMap<String, CacheSettings>()

    @Suppress("UNCHECKED_CAST")
    fun <K : Any, V : Any> get(cacheName: String = DEFAULT_CACHE_NAME): Cache<K, V>? {
        return (cachesMap[cacheName] ?: run {
            val settings = settingsMap[cacheName] ?: defSettings.first()
            builder.build<K, V>(settings)?.also { cachesMap[cacheName] = it }
        }) as? Cache<K, V>
    }

    suspend fun clean(cacheName: String = DEFAULT_CACHE_NAME) {
        cachesMap[cacheName]?.evictAll()
    }

    suspend fun cleanAll() {
        cachesMap.values.forEach { it.evictAll() }
    }

    init {
        settings.forEach { settingsMap[it.cacheName] = it }
    }

    companion object {
        const val DEFAULT_CACHE_NAME = "DEFAULT_CACHE_38e43d05t8y0"

        private val defSettings = arrayOf(
            CacheSettings(
                cacheName = DEFAULT_CACHE_NAME,
                capacity = 1000,
                eternal = true,
            )
        )
    }
}