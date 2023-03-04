package com.example.orbitmvisample.ui.alert

interface AlertListener {
    fun onOpen(alert: Alert)
    fun onClose(alert: Alert)
}