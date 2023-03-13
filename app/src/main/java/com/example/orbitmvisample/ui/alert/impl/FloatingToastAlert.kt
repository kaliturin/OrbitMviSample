package com.example.orbitmvisample.ui.alert.impl

import android.view.View
import androidx.annotation.MainThread
import com.example.orbitmvisample.ui.alert.Alert
import com.example.orbitmvisample.ui.alert.AlertData
import com.example.orbitmvisample.ui.alert.AlertListener
import com.example.orbitmvisample.ui.alert.impl.view.FloatingToast

class FloatingToastAlert(
    parent: View,
    private val alertData: AlertData,
    private val withCallbacks: Boolean = false
) : Alert {

    override val id = alertData.id
    private var alertListener: AlertListener? = null
    private var toast: FloatingToast

    @MainThread
    override fun show() {
        toast.show(alertData.getMessage()) {
            alertListener?.onOpen(this)
        }
    }

    @MainThread
    override fun hide(): Boolean {
        toast.hide {
            alertListener?.onClose(this)
        }
        return true
    }

    override fun isShowing(): Boolean = toast.isShowing()

    override fun alertListener(alertListener: AlertListener?): Boolean {
        if (withCallbacks) {
            this.alertListener = alertListener
            return true
        }
        return false
    }

    init {
        val settings = (alertData.specificSettings as? FloatingToast.Settings)
        toast = settings?.let { FloatingToast(parent, alertData.alertStyle, it) }
            ?: FloatingToast(parent, alertData.alertStyle)
    }
}