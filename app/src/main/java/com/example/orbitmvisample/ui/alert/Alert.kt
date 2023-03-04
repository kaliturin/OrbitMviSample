package com.example.orbitmvisample.ui.alert

import androidx.annotation.MainThread

interface Alert {
    /**
     * Alert's id
     */
    val id: AlertData.Id

    /**
     * Shows an alert
     */
    @MainThread
    fun show()

    /**
     * Hides an alert
     * @return true on success
     */
    @MainThread
    fun hide(): Boolean = false

    /**
     * Adds alert's listener
     * @return true if the alert supports listeners
     */
    fun alertListener(alertListener: AlertListener?): Boolean = false
}