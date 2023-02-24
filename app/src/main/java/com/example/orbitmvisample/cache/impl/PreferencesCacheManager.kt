package com.example.orbitmvisample.cache.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.appmattus.layercache.Cache
import com.example.orbitmvisample.cache.crypto.CryptoManager
import org.jetbrains.annotations.ApiStatus.Internal
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/**
 * This cache manager provides [com.appmattus.layercache.Cache]<String, V> cache
 * that stores values in [androidx.datastore.core.DataStore]<Preferences>
 */
class PreferencesCacheManager(
    private val context: Context,
    val cryptoManager: CryptoManager
) {
    private val dataStoreMap = ConcurrentHashMap<String, DataStore<Preferences>>()

    inline fun <reified V : Any> get(cacheName: String = DEFAULT_CACHE_NAME): Cache<String, V>? {
        return getDataStore(cacheName)?.asCryptoJsonCache(cryptoManager, V::class)
    }

    suspend fun clean(cacheName: String = DEFAULT_CACHE_NAME) {
        getDataStore(cacheName)?.asStringCache()?.evictAll()
    }

    @Internal
    fun getDataStore(cacheName: String = DEFAULT_CACHE_NAME): DataStore<Preferences>? {
        return try {
            dataStoreMap[cacheName] ?: run {
                PreferencesDataStore(cacheName).get(context).also {
                    dataStoreMap[cacheName] = it
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    private class PreferencesDataStore(name: String) {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name)
        fun get(context: Context): DataStore<Preferences> {
            return context.dataStore
        }
    }

    companion object {
        const val DEFAULT_CACHE_NAME = "default_cache_68e0fg547n60"
    }
}