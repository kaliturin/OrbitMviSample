package com.example.orbitmvisample.ui.helpers.logger

import android.util.Log

/**
 * Log message holder
 * @param message message text
 * @param priority log priority one of [Log.VERBOSE],[Log.DEBUG],[Log.INFO],[Log.WARN],[Log.ERROR],[Log.ASSERT]
 */
data class LogMessage(
    val message: String,
    val priority: Int = Log.DEBUG
)