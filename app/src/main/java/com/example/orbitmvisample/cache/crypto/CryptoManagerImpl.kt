package com.example.orbitmvisample.cache.crypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.example.orbitmvisample.BuildConfig
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class CryptoManagerImpl : CryptoManager {

    private val keyStore = KeyStore.getInstance(KEYSTORE_TYPE)
        .apply { load(null) }

    private fun generateKey(): SecretKey {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(false)
                .build()
            return KeyGenerator.getInstance(ALGORITHM, KEYSTORE_TYPE).apply {
                init(keyGenParameterSpec)
            }.generateKey()
        } else {
            var key = KEY_ALIAS.toByteArray(StandardCharsets.UTF_8)
            val sha = MessageDigest.getInstance("SHA-256")
            sha.reset()
            key = sha.digest(key)
            return SecretKeySpec(key, ALGORITHM)
        }
    }

    private fun getKey(): SecretKey {
        val existingKey = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: generateKey()
    }

    private fun getEncryptCipher(): Cipher {
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getKey())
        }
    }

    private fun getDecryptCipher(iv: ByteArray): Cipher {
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv))
        }
    }

    override fun encrypt(bytes: ByteArray, outputStream: OutputStream): ByteArray {
        val cipher = getEncryptCipher()
        val encryptedBytes = cipher.doFinal(bytes)
        outputStream.use {
            it.write(cipher.iv.size)
            it.write(cipher.iv)
            it.write(encryptedBytes.size)
            it.write(encryptedBytes)
        }
        return encryptedBytes
    }

    override fun decrypt(inputStream: InputStream): ByteArray {
        return inputStream.use { input ->
            val size = input.read()
            val iv = ByteArray(size)
            input.read(iv)
            val encryptedBytesSize = input.read()
            val encryptedBytes = ByteArray(encryptedBytesSize)
            input.read(encryptedBytes)
            val cipher = getDecryptCipher(iv)
            cipher.doFinal(encryptedBytes)
        }
    }

    override fun encrypt(data: String): String {
        val cipher = getEncryptCipher()
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(data.toByteArray(Charset.defaultCharset()))
        return Base64.encodeToString(iv + ciphertext, Base64.DEFAULT)
    }

    override fun decrypt(data: String): String {
        val decodedData = Base64.decode(data, Base64.DEFAULT)
        val iv = decodedData.sliceArray(0..15)
        val ciphertext = decodedData.sliceArray(16 until decodedData.size)
        val cipher = getDecryptCipher(iv)
        return cipher.doFinal(ciphertext)
            .toString(Charset.defaultCharset())
    }

    companion object {
        private const val KEY_ALIAS = "alias_${BuildConfig.APPLICATION_ID}_0"
        private const val KEYSTORE_TYPE = "AndroidKeyStore"
        private const val ALGORITHM = "AES"
        private const val BLOCK_MODE = "CBC"
        private const val PADDING = "PKCS7Padding"
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }
}