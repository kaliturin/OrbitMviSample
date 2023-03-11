package com.example.orbitmvisample.eventbus

import android.os.SystemClock
import kotlin.reflect.KClass

class EventTimeoutManager(
    @PublishedApi
    internal val defTimeout: Long = 1000
) {
    @PublishedApi
    internal val timeoutList = mutableListOf<EventTimeout>()

    inline fun <reified T : Any> register(timeout: Long = defTimeout) = apply {
        timeoutList.add(EventTimeout(T::class, timeout))
    }

    fun isTimeout(e: Any): Boolean {
        return timeoutList.any { it.isTimeout(e) }
    }
}

@PublishedApi
internal class EventTimeout(
    private val clazz: KClass<*>,
    private val timeout: Long
) {
    private var elapsed: Long = 0

    fun isTimeout(event: Any?): Boolean {
        if (clazz.isInstance(event)) {
            val time = SystemClock.elapsedRealtime()
            if (elapsed > 0 && time - elapsed < timeout) return true
            elapsed = time
        }
        return false
    }
}