package com.example.orbitmvisample.cache

import com.appmattus.layercache.Cache
import kotlin.reflect.KClass

interface CacheBuilder {
    fun <K : Any, V : Any> build(settings: CacheSettings, clazz: KClass<V>): Cache<K, V>?
}