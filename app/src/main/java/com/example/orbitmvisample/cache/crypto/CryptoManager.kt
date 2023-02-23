package com.example.orbitmvisample.cache.crypto

import java.io.InputStream
import java.io.OutputStream

interface CryptoManager {
    fun encrypt(bytes: ByteArray, outputStream: OutputStream): ByteArray
    fun decrypt(inputStream: InputStream): ByteArray
    fun encrypt(data: String): String
    fun decrypt(data: String): String
}