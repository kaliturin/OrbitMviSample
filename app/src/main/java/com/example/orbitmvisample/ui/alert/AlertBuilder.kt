package com.example.orbitmvisample.ui.alert

import android.content.Context

interface AlertBuilder {
    fun build(context: Context, data: AlertData): Alert
}