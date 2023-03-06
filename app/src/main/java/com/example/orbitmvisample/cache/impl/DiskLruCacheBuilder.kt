package com.example.orbitmvisample.cache.impl

import android.content.Context
import com.appmattus.layercache.Cache
import com.appmattus.layercache.createDiskLruCache
import com.appmattus.layercache.encrypt
import com.example.orbitmvisample.cache.CacheBuilder
import com.example.orbitmvisample.cache.CacheSettings
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.reflect.KClass

/**
 * J.Wharton's [com.jakewharton.disklrucache.DiskLruCache] wrapped by
 * [com.appmattus.layercache.Cache] timed cache builder
 */
class DiskLruCacheBuilder(private val context: Context) : CacheBuilder {
    override fun <K : Any, V : Any> build(settings: CacheSettings, clazz: KClass<V>): Cache<K, V>? {
        val size = settings.size.let { if (it > 0) it else MIN_SIZE }

        return context.getExternalFilesDir(settings.cacheName + EXT)?.let { file ->
            val cache = try {
                DiskLruCacheWrapper(file, size)
            } catch (e: Exception) {
                Timber.e(e)
                return null
            }.encrypt(context)

            if (settings.eternal || settings.timeToExpire <= 0)
                cache.asJsonCache(clazz)
            else
                cache.asTimedJsonCache(settings)
        }
    }

    companion object {
        private const val MIN_SIZE = 1024L
        private const val EXT = ".lru"
    }
}

/**
 * This wrapper helps to avoid "java.lang.IllegalArgumentException: keys must match regex [a-z0-9_-]{1,64}"
 * when using J.Wharton's DiskLruCache
 */
private class DiskLruCacheWrapper(file: File, size: Long) : Cache<String, String> {

    private val cache = Cache.createDiskLruCache(file, size)

    override suspend fun evict(key: String) {
        cache.evict(restrict(key))
    }

    override suspend fun evictAll() {
        return cache.evictAll()
    }

    override suspend fun get(key: String): String? {
        return cache.get(restrict(key))
    }

    override suspend fun set(key: String, value: String) {
        cache.set(restrict(key), value)
    }

    // Restricts the passed string key to DiskLruCache key requirements
    private fun restrict(key: String): String {
        val k = (if (key.length > MAX_LENGTH)
            key.substring(0, MAX_LENGTH) else key) // restrict length up to 64
            .lowercase(Locale.getDefault()) // replace case
        // somehow kotlin's String.replace() couldn't replace "/" and "+" (at least)
        return java.lang.String(k)
            .replaceAll("[^a-z0-9_-]", "_") // replace not permitted chars with "_"
    }

    companion object {
        private const val MAX_LENGTH = 64
    }
}


