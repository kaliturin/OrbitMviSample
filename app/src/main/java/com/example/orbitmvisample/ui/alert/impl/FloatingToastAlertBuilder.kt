package com.example.orbitmvisample.ui.alert.impl

import android.content.Context
import android.view.View
import com.example.orbitmvisample.ui.alert.Alert
import com.example.orbitmvisample.ui.alert.AlertBuilder
import com.example.orbitmvisample.ui.alert.AlertData

class FloatingToastAlertBuilder(private val parent: View) : AlertBuilder {
    override fun build(context: Context, data: AlertData): Alert {
        return FloatingToastAlert(parent, data)
    }
}

