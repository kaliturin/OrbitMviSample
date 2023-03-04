package com.example.orbitmvisample.ui.alert.impl

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.MainThread
import com.example.orbitmvisample.ui.alert.Alert
import com.example.orbitmvisample.ui.alert.AlertData
import com.example.orbitmvisample.ui.alert.AlertListener

class ToastAlert(
    private val context: Context,
    private val alertData: AlertData
) : Alert {
    override val id = alertData.id
    private var alertListener: AlertListener? = null

    /**
     * Since SDK 30
     */
    override fun alertListener(alertListener: AlertListener?): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.alertListener = alertListener
            true
        } else false
    }

    @MainThread
    override fun show() {
        val duration = if (alertData.durationMills > MILLS)
            Toast.LENGTH_LONG
        else
            Toast.LENGTH_SHORT
        val toast = Toast.makeText(context, alertData.message, duration)
        toast.show()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            toast.addCallback(object : Toast.Callback() {
                override fun onToastShown() {
                    alertListener?.onOpen(this@ToastAlert)
                }

                override fun onToastHidden() {
                    alertListener?.onClose(this@ToastAlert)
                }
            })
        }
    }

    companion object {
        private const val MILLS = 1500
    }
}

