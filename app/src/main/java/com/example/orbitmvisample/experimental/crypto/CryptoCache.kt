package com.example.orbitmvisample.experimental.crypto

import com.appmattus.layercache.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

fun Cache<String, String>.asCryptoCache(cryptoManager: CryptoManager): Cache<String, String> =
    CryptoCache(cryptoManager, this)

/**
 * Encodes and decodes the wrapped [com.appmattus.layercache.Cache]<String, String>
 * cache's values with crypto manager
 */
class CryptoCache(
    private val cryptoManager: CryptoManager,
    private val cache: Cache<String, String>
) : Cache<String, String> {

    override suspend fun evict(key: String) {
        cache.evict(key)
    }

    override suspend fun evictAll() {
        return cache.evictAll()
    }

    override suspend fun get(key: String): String? {
        return withContext(Dispatchers.Default) {
            cache.get(key)?.let { decrypt(it) }
        }
    }

    override suspend fun set(key: String, value: String) {
        withContext(Dispatchers.Default) {
            encrypt(value)?.let { cache.set(key, it) }
        }
    }

    private fun encrypt(value: String): String? {
        return try {
            cryptoManager.encrypt(value)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    private fun decrypt(value: String): String? {
        return try {
            cryptoManager.decrypt(value)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }
}