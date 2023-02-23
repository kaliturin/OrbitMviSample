package com.example.orbitmvisample.cache.impl

import com.appmattus.layercache.Cache
import com.example.orbitmvisample.utils.JsonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass

/**
 * Json wrapper for cache [com.appmattus.layercache.Cache]<String, String>.
 * Allows to store in the cache arbitrary json-serializing object.
 */
class JsonCache<T : Any>(
    private val clazz: KClass<T>,
    private val cache: Cache<String, String>
) : Cache<String, T> {

    override suspend fun evict(key: String) {
        return cache.evict(key)
    }

    override suspend fun evictAll() {
        return cache.evictAll()
    }

    override suspend fun get(key: String): T? {
        return withContext(Dispatchers.Default) {
            val json = cache.get(key)
            JsonUtils.fromJsonSafe(json, clazz)
        }
    }

    override suspend fun set(key: String, value: T) {
        withContext(Dispatchers.Default) {
            val json = JsonUtils.toJsonSafe(value) ?: ""
            cache.set(key, json)
        }
    }
}