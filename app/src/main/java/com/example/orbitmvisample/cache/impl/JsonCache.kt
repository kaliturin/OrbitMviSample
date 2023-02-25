package com.example.orbitmvisample.cache.impl

import com.appmattus.layercache.Cache
import com.example.orbitmvisample.utils.JsonUtils.fromJsonSafe
import com.example.orbitmvisample.utils.JsonUtils.toJsonSafe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass

inline fun <K : Any, reified V : Any> Cache<String, String>.asJsonCache(): Cache<K, V> =
    JsonCache(V::class, this)

fun <K : Any, V : Any> Cache<String, String>.asJsonCache(clazz: KClass<V>): Cache<K, V> =
    JsonCache(clazz, this)

/**
 * Json wrapper for cache [com.appmattus.layercache.Cache]<String, String>.
 * Allows to store in the cache arbitrary json-serializing object.
 */
@PublishedApi
internal class JsonCache<K : Any, V : Any>(
    private val clazz: KClass<V>,
    private val cache: Cache<String, String>
) : Cache<K, V> {

    override suspend fun evict(key: K) {
        withContext(Dispatchers.Default) {
            toJsonSafe(key)?.let { keyJson ->
                cache.evict(keyJson)
            }
        }
    }

    override suspend fun evictAll() {
        return cache.evictAll()
    }

    override suspend fun get(key: K): V? {
        return withContext(Dispatchers.Default) {
            toJsonSafe(key)?.let { keyJson ->
                val valueJson = cache.get(keyJson)
                fromJsonSafe(valueJson, clazz)
            }
        }
    }

    override suspend fun set(key: K, value: V) {
        withContext(Dispatchers.Default) {
            toJsonSafe(key)?.let { keyJson ->
                toJsonSafe(value)?.let { valueJson ->
                    cache.set(keyJson, valueJson)
                }
            }
        }
    }
}

