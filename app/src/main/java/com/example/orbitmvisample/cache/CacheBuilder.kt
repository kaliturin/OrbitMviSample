package com.example.orbitmvisample.cache

import com.appmattus.layercache.Cache

interface CacheBuilder<K : Any, V : Any> {
    fun build(settings: CacheSettings): Cache<K, V>
}