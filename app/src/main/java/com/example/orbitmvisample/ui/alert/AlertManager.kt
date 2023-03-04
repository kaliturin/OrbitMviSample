package com.example.orbitmvisample.ui.alert

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.core.os.postDelayed
import com.example.orbitmvisample.ui.alert.impl.ToastAlertBuilder
import java.lang.ref.WeakReference


class AlertManager(
    private val context: Context? = null,
    private var alertBuilder: AlertBuilder = ToastAlertBuilder(),
    private var repeatingAlerts: Boolean = false
) {
    private val timerHandler = Handler(Looper.getMainLooper())
    private var showingAlert: WeakReference<Alert>? = null

    fun setRepeatingAlerts(repeatingAlerts: Boolean) = apply {
        this.repeatingAlerts = repeatingAlerts
    }

    fun setAlertBuilder(alertBuilder: AlertBuilder) = apply {
        this.alertBuilder = alertBuilder
    }

    fun showAlert(data: AlertData) = showAlert(context, data)

    fun showAlert(context: Context?, data: AlertData) {
        if (context == null || !repeatingAlerts && showingAlert?.get()?.id == data.id) return
        val alert = (data.alertBuilder ?: alertBuilder)
            .build(context, data)
        if (!alert.alertListener(alertListener)) {
            countTimeUntilAlertHide(data.durationMills, alert)
        }
        runOnUiThread { alert.show() }
    }

    private val alertListener = object : AlertListener {
        override fun onOpen(alert: Alert) {
            showingAlert = WeakReference(alert)
        }

        override fun onClose(alert: Alert) {
            showingAlert = null
        }
    }

    private fun countTimeUntilAlertHide(durationMills: Long, alert: Alert) {
        if (durationMills <= 0L) return
        timerHandler.removeCallbacksAndMessages(null)
        showingAlert = WeakReference(alert)
        timerHandler.postDelayed(durationMills) {
            runOnUiThread { showingAlert?.get()?.hide() }
            showingAlert = null
        }
    }

    private fun runOnUiThread(callback: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper())
            callback()
        else
            timerHandler.post(callback)
    }
}