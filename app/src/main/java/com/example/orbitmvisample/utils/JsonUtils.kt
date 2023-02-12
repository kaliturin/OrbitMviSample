package com.example.orbitmvisample.utils

import android.text.TextUtils
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import timber.log.Timber
import java.io.IOException
import kotlin.reflect.KClass

object JsonUtils {

    private val mapper: ObjectMapper = JsonMapper.builder()
        .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        .build()
        .registerKotlinModule()

    fun <T : Any> fromJson(json: String?, clazz: KClass<T>): T? {
        return fromJson(json, clazz.java)
    }

    /**
     * Converts the passed json string to a object of the passed Class
     */
    fun <T> fromJson(json: String?, clazz: Class<T>): T? {
        return if (!TextUtils.isEmpty(json)) {
            try {
                mapper.readerFor(clazz).readValue(json)
            } catch (e: IOException) {
                Timber.e(e)
                throw RuntimeException(e)
            }
        } else null
    }
}