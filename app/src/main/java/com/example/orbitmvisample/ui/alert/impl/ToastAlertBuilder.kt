package com.example.orbitmvisample.ui.alert.impl

import android.content.Context
import com.example.orbitmvisample.ui.alert.Alert
import com.example.orbitmvisample.ui.alert.AlertBuilder
import com.example.orbitmvisample.ui.alert.AlertData

class ToastAlertBuilder : AlertBuilder {
    override fun build(context: Context, data: AlertData): Alert {
        return ToastAlert(context, data)
    }
}

