package com.example.orbitmvisample.cache.crypto

import com.appmattus.layercache.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Encodes and decodes the wrapped [com.appmattus.layercache.Cache]<String, String> cache's values
 */
class CryptoCache(
    private val cryptoManager: CryptoManager,
    private val cache: Cache<String, String>
) : Cache<String, String> {

    override suspend fun evict(key: String) {
        return cache.evict(key)
    }

    override suspend fun evictAll() {
        return cache.evictAll()
    }

    override suspend fun get(key: String): String? {
        return cache.get(key)?.let { value ->
            withContext(Dispatchers.Default) {
                try {
                    cryptoManager.decrypt(value)
                } catch (e: Exception) {
                    Timber.e(e)
                    null
                }
            }
        }
    }

    override suspend fun set(key: String, value: String) {
        val encrypted = withContext(Dispatchers.Default) {
            try {
                cryptoManager.encrypt(value)
            } catch (e: Exception) {
                Timber.e(e)
                ""
            }
        }
        cache.set(key, encrypted)
    }
}