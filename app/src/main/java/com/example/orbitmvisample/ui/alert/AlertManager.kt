package com.example.orbitmvisample.ui.alert

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.collection.LruCache
import androidx.core.os.postDelayed
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.example.orbitmvisample.AppContext
import com.example.orbitmvisample.ui.alert.impl.ToastAlertBuilder
import timber.log.Timber
import java.lang.ref.WeakReference

/**
 * Alerts manager allows to build, show, hide and other managing alert messages
 */
class AlertManager(
    private val context: Context? = null,
    private var defAlertBuilder: AlertBuilder = ToastAlertBuilder()
) {
    private val timerHandler = Handler(Looper.getMainLooper())
    private val timedAlertsContainer = TimedAlertsContainer()

    fun setDefAlertBuilder(builder: AlertBuilder) = apply {
        this.defAlertBuilder = builder
    }

    fun onDestroy() {
        timerHandler.removeCallbacksAndMessages(null)
    }

    fun showAlert(data: AlertData) = showAlert(context, data)

    fun showAlert(context: Context?, data: AlertData) {
        context ?: return

        // if parent isn't resumed - don't show the alert
        if (data.showOnlyIfParentIsResumed &&
            context is FragmentActivity &&
            !context.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        ) return

        // suppress the  repeating alerts if needed
        if (timedAlertsContainer.isRepeating(data)) return

        // build a new alert
        val alert = (data.alertBuilder ?: defAlertBuilder)
            .build(context, data)

        // save alert if it can't be repeated
        if (data.repeatingMills > 0) timedAlertsContainer.put(alert)

        // attach listener or count down showing duration time
        if (!alert.alertListener(alertListener))
            countDownTimeToHideAlert(data.durationMills, alert)

        // show the alert in main thread
        runOnUiThread { safeCall { alert.show() } }
    }

    private val alertListener = object : AlertListener {
        override fun onOpen(alert: Alert) {
        }

        override fun onClose(alert: Alert) {
            timedAlertsContainer.clean(alert.id)
        }
    }

    private fun countDownTimeToHideAlert(durationMills: Long, alert: Alert) {
        if (durationMills <= 0L) return
        timerHandler.removeCallbacksAndMessages(alert.id)
        timerHandler.postDelayed(durationMills, alert.id) {
            timedAlertsContainer.clean(alert.id)
            // hide the alert in main thread
            runOnUiThread { safeCall { alert.hide() } }
        }
    }

    private fun runOnUiThread(callback: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper())
            callback()
        else
            timerHandler.post(callback)
    }

    private fun safeCall(callback: () -> Unit) {
        try {
            callback()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        val instance = AlertManager(context = AppContext)
    }
}

/**
 * Container of alerts that have a limited timeout of repeating and are currently showing
 */
private class TimedAlertsContainer {
    private var showingAlerts =
        LruCache<AlertData.Id, WeakReference<TimedAlert>>(MAX_TIMED_ALERTS)

    @Synchronized
    fun isRepeating(data: AlertData): Boolean {
        // if suppress repeating alerts - check the repeating timeout
        return if (data.repeatingMills > 0) {
            val timed = find(data.id)
            val alert = timed?.alert
            alert?.isShowing() == true &&
                    data.repeatingMills > SystemClock.elapsedRealtime() - timed.time
        } else false
    }

    @Synchronized
    fun put(alert: Alert) {
        val timed = TimedAlert(SystemClock.elapsedRealtime(), alert)
        showingAlerts.put(alert.id, WeakReference(timed))
    }

    @Synchronized
    fun clean(id: AlertData.Id) {
        showingAlerts.remove(id)
    }

    private fun find(id: AlertData.Id): TimedAlert? {
        val ref = showingAlerts[id]
        val alert = ref?.get()
        if (ref != null && alert == null) showingAlerts.remove(id)
        return alert
    }

    private class TimedAlert(val time: Long, val alert: Alert)

    companion object {
        const val MAX_TIMED_ALERTS = 3
    }
}