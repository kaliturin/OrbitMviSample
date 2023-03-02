package com.example.orbitmvisample.apierrorhandler.impl

import android.os.Bundle

class AppErrorHandlerSettingsHelper(private val defSettings: Bundle = Bundle()) {
    private var settings: Bundle? = null

    fun setCurrentSettings(settings: Bundle?) {
        this.settings = settings
    }

    private fun Bundle?.getBooleanOrNull(name: String): Boolean? {
        return if (this?.containsKey(name) == true) getBoolean(name) else null
    }

    fun getBoolean(name: String): Boolean {
        return settings?.getBooleanOrNull(name) ?: defSettings.getBoolean(name)
    }
}