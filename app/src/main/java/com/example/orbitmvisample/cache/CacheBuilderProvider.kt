package com.example.orbitmvisample.cache

import android.content.Context
import com.example.orbitmvisample.cache.impl.Cache2KBuilder
import com.example.orbitmvisample.cache.impl.LruCacheBuilder
import com.example.orbitmvisample.cache.impl.PreferencesCacheBuilder
import java.util.concurrent.ConcurrentHashMap

class CacheBuilderProvider(private val context: Context) {
    private val builders = ConcurrentHashMap<CacheType, CacheBuilder>()

    fun get(settings: CacheSettings): CacheBuilder {
        return builders[settings.cacheType]
            ?: run {
                build(settings.cacheType).also {
                    builders[settings.cacheType] = it
                }
            }
    }

    private fun build(cacheType: CacheType): CacheBuilder {
        return when (cacheType) {
            CacheType.LRU -> LruCacheBuilder()
            CacheType.CACHE2K -> Cache2KBuilder()
            CacheType.PREFERENCES -> PreferencesCacheBuilder(context)
        }
    }
}