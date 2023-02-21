package com.example.orbitmvisample.cache.impl

import com.appmattus.layercache.Cache
import com.example.orbitmvisample.utils.JsonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass

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
        val json = cache.get(key)
        return withContext(Dispatchers.Default) {
            JsonUtils.fromJsonSafe(json, clazz)
        }
    }

    override suspend fun set(key: String, value: T) {
        val json = withContext(Dispatchers.Default) {
            JsonUtils.toJsonSafe(value) ?: ""
        }
        cache.set(key, json)
    }
}