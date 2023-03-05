package com.example.orbitmvisample.eventbus

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.koin.java.KoinJavaComponent.inject

@Suppress("unused")
class EventBusObserver(private val config: Config = Config.onCreateOnDestroyConfig) :
    DefaultLifecycleObserver {
    private val cacheManager: EventBusManager by inject(EventBusManager::class.java)

    class Config(
        val onCreateOnDestroy: Boolean = false,
        val onStartOnStop: Boolean = false,
        val onResumeOnPause: Boolean = false
    ) {
        companion object {
            val onCreateOnDestroyConfig = Config(onCreateOnDestroy = true)
            val onStartOnStopConfig = Config(onStartOnStop = true)
            val onResumeOnPauseConfig = Config(onResumeOnPause = true)
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        if (config.onCreateOnDestroy) cacheManager.register(owner)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        if (config.onCreateOnDestroy) cacheManager.unregister(owner)
    }

    override fun onStart(owner: LifecycleOwner) {
        if (config.onStartOnStop) cacheManager.register(owner)
    }

    override fun onStop(owner: LifecycleOwner) {
        if (config.onStartOnStop) cacheManager.unregister(owner)
    }

    override fun onResume(owner: LifecycleOwner) {
        if (config.onResumeOnPause) cacheManager.register(owner)
    }

    override fun onPause(owner: LifecycleOwner) {
        if (config.onResumeOnPause) cacheManager.unregister(owner)
    }
}