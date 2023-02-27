package com.example.orbitmvisample.cache.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.appmattus.layercache.Cache
import com.appmattus.layercache.encrypt
import com.example.orbitmvisample.cache.CacheBuilder
import com.example.orbitmvisample.cache.CacheSettings
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set
import kotlin.reflect.KClass

/**
 * Builds [com.appmattus.layercache.Cache] cache that is wrapping
 * [androidx.datastore.core.DataStore]<Preferences> cache
 */
class PreferencesCacheBuilder(
    private val context: Context
) : CacheBuilder {
    private val dataStoreMap = ConcurrentHashMap<String, DataStore<Preferences>>()

    override fun <K : Any, V : Any> build(settings: CacheSettings, clazz: KClass<V>): Cache<K, V>? {
        val cache = getDataStore(settings.cacheName)
            ?.asStringCache()
            ?.encrypt(context)

        return if (settings.timeToExpire >= 0L)
            cache?.asTimedJsonCache(settings)
        else
            cache?.asJsonCache(clazz)
    }

    private fun getDataStore(cacheName: String): DataStore<Preferences>? {
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
}