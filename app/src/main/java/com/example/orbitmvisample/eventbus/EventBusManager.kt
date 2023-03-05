package com.example.orbitmvisample.eventbus

import com.example.orbitmvisample.MyEventBusIndex
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import kotlin.reflect.KClass

@Suppress("unused", "unused_parameter")
class EventBusManager {

    private val eventBus = EventBus.builder()
        .addIndex(MyEventBusIndex())
        .logNoSubscriberMessages(false)
        .sendNoSubscriberEvent(false)
        .build()

    fun register(subscriber: Any?) {
        eventBus.apply {
            if (!isRegistered(subscriber)) register(subscriber)
        }
    }

    fun unregister(subscriber: Any?) {
        eventBus.apply {
            if (isRegistered(subscriber)) unregister(subscriber)
        }
    }

    fun post(event: Any) {
        eventBus.post(event)
    }

    fun postSticky(event: Any) {
        eventBus.postSticky(event)
    }

    fun <T : Any> removeSticky(stickyEventClass: KClass<T>) {
        eventBus.getStickyEvent(stickyEventClass.java)?.let {
            eventBus.removeStickyEvent(it)
        }
    }

    // This is needed just for generating the MyEventBusIndex class at first time
    @Subscribe
    fun onEvent(event: Event) {
    }
}