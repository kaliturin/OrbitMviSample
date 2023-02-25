package com.example.orbitmvisample.experimental.crypto

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.core.Serializer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

@RequiresApi(Build.VERSION_CODES.M)
class CryptoSerializer<T : Any>(
    private val cryptoManager: CryptoManager,
    private val clazz: KClass<T>
) : Serializer<T> {

    override val defaultValue: T
        get() = clazz.createInstance()

    @OptIn(InternalSerializationApi::class)
    override suspend fun readFrom(input: InputStream): T {
        return try {
            val json = cryptoManager.decrypt(input).decodeToString()
            Json.decodeFromString(clazz.serializer(), json)
        } catch (e: Exception) {
            Timber.e(e)
            defaultValue
        }
    }

    @OptIn(InternalSerializationApi::class)
    override suspend fun writeTo(t: T, output: OutputStream) {
        try {
            val json = Json.encodeToString(clazz.serializer(), t)
            cryptoManager.encrypt(json.encodeToByteArray(), output)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}