package com.example.orbitmvisample.cache

import com.appmattus.layercache.Cache
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/**
 * Cache manager allows to obtain [com.appmattus.layercache.Cache]<K, V> cache,
 * that is built by passed cache builder with settings.
 */
class CacheManager(
    val cacheBuilder: CacheBuilder? = null,
    val cacheBuilderProvider: CacheBuilderProvider? = null,
    vararg settings: CacheSettings = defSettings
) {
    @PublishedApi
    internal val cachesMap = ConcurrentHashMap<String, Cache<*, *>>()

    @PublishedApi
    internal val settingsMap = mutableMapOf<String, CacheSettings>()

    @Suppress("unchecked_cast")
    @PublishedApi
    internal inline fun <K : Any, reified V : Any> getInternal(cacheName: String = DEFAULT_CACHE_NAME): Cache<K, V>? {
        synchronized(this) {
            return try {
                (cachesMap[cacheName] ?: run {
                    val settings = settingsMap[cacheName] ?: defSettings.first()
                    val builder = cacheBuilder ?: cacheBuilderProvider?.get(settings)
                    ?: throw IllegalArgumentException("No CacheBuilder is provided")
                    builder.build<K, V>(settings, V::class)?.also { cachesMap[cacheName] = it }
                }) as? Cache<K, V>
            } catch (e: Exception) {
                Timber.e(e)
                null
            }
        }
    }

    inline fun <reified V : Any> get(cacheName: String = DEFAULT_CACHE_NAME): Cache<Any, V>? =
        getInternal(cacheName)

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
        const val DEFAULT_CACHE_NAME = "default_cache_38e43d05t8y0"

        @PublishedApi
        internal val defSettings = arrayOf(
            CacheSettings(
                cacheName = DEFAULT_CACHE_NAME,
                capacity = 1000,
                eternal = true,
            )
        )
    }
}