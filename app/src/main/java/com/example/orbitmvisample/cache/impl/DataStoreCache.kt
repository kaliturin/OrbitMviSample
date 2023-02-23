package com.example.orbitmvisample.cache.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.appmattus.layercache.Cache
import com.example.orbitmvisample.cache.crypto.CryptoCache
import com.example.orbitmvisample.cache.crypto.CryptoManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import kotlin.reflect.KClass

fun <T : Any> DataStore<Preferences>.asCryptoJsonCache(
    cryptoManager: CryptoManager, clazz: KClass<T>
): Cache<String, T> = JsonCache(clazz, CryptoCache(cryptoManager, asStringCache()))

fun <T : Any> DataStore<Preferences>.asJsonCache(clazz: KClass<T>): Cache<String, T> =
    JsonCache(clazz, asStringCache())

fun DataStore<Preferences>.asStringCache(): Cache<String, String> =
    DataStoreCache(this) { stringPreferencesKey(it) }

fun DataStore<Preferences>.asStringSetCache(): Cache<String, Set<String>> =
    DataStoreCache(this) { stringSetPreferencesKey(it) }

fun DataStore<Preferences>.asIntCache(): Cache<String, Int> =
    DataStoreCache(this) { intPreferencesKey(it) }

fun DataStore<Preferences>.asLongCache(): Cache<String, Long> =
    DataStoreCache(this) { longPreferencesKey(it) }

fun DataStore<Preferences>.asBooleanCache(): Cache<String, Boolean> =
    DataStoreCache(this) { booleanPreferencesKey(it) }

fun DataStore<Preferences>.asFloatCache(): Cache<String, Float> =
    DataStoreCache(this) { floatPreferencesKey(it) }

fun DataStore<Preferences>.asDoubleCache(): Cache<String, Double> =
    DataStoreCache(this) { doublePreferencesKey(it) }

/**
 * [androidx.datastore.core.DataStore]<Preferences> wrapped with [com.appmattus.layercache.Cache]<String, T>
 */
private class DataStoreCache<T : Any>(
    private val store: DataStore<Preferences>,
    private val getKey: (String) -> Preferences.Key<T>
) : Cache<String, T> {

    override suspend fun get(key: String): T? {
        return try {
            store.data.map { it[getKey(key)] }.first()
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    override suspend fun set(key: String, value: T) {
        try {
            store.edit { it[getKey(key)] = value }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override suspend fun evict(key: String) {
        try {
            store.edit { it.remove(getKey(key)) }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override suspend fun evictAll() {
        try {
            store.edit { it.clear() }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}