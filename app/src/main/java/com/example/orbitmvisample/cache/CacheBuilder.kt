package com.example.orbitmvisample.cache

import com.appmattus.layercache.Cache

interface CacheBuilder {
    fun build(settings: CacheSettings): Cache<Any, Any>
}