package com.example.orbitmvisample.cache

import com.appmattus.layercache.Cache

interface CacheBuilder {
    fun <K : Any, V : Any> build(settings: CacheSettings): Cache<K, V>?
}