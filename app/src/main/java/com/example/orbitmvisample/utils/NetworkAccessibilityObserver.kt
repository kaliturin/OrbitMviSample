package com.example.orbitmvisample.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.os.postDelayed
import com.example.orbitmvisample.AppContext
import com.example.orbitmvisample.R
import com.example.orbitmvisample.eventbus.Event
import com.example.orbitmvisample.eventbus.EventBusManager
import com.example.orbitmvisample.ui.alert.AlertBuilder
import com.example.orbitmvisample.ui.alert.AlertData
import com.example.orbitmvisample.ui.alert.AlertManager
import com.example.orbitmvisample.ui.alert.impl.ToastAlertBuilder
import timber.log.Timber

/**
 * A network accessibility observer.
 * Sends broadcast Event.NetworkStatusChanged on every network status change.
 */
class NetworkAccessibilityObserver(
    private val eventBusManager: EventBusManager,
    private val alertManager: AlertManager? = AlertManager.instance,
    private val alertBuilder: AlertBuilder? = ToastAlertBuilder()
) {
    private val service =
        AppContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val debounceHandler = Handler(Looper.getMainLooper())
    private var isRegistered = false

    /**
     * Starts to observe a network accessibility
     */
    fun observe(): Boolean {
        if (!isRegistered && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            service?.registerDefaultNetworkCallback(networkCallback)
            isRegistered = true
        }
        return isRegistered
    }

    fun destroy() {
        debounceHandler.removeCallbacksAndMessages(null)
        service?.unregisterNetworkCallback(networkCallback)
        isRegistered = false
    }

    private fun onNetworkStatusChange(network: Network, event: Event.NetworkStatusChanged) {
        debounceHandler.removeCallbacksAndMessages(null)
        val delay = if (event.isAvailable) STATUS_CHANGE_DELAY else 0
        debounceHandler.postDelayed(delay) {
            if (event.isAvailable)
                Timber.d("Network %s connection is available", network)
            else
                Timber.d("Network %s connection is lost", network)
            onNetworkStatusChangedInternal(event)
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            onNetworkStatusChange(network, Event.NetworkStatusChanged(true))
        }

        override fun onLost(network: Network) {
            onNetworkStatusChange(network, Event.NetworkStatusChanged(false))
        }
    }

    @Suppress("DEPRECATION")
    private fun isNetworkAvailable2(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            service?.activeNetworkInfo?.isConnected ?: false
        } else {
            service?.activeNetwork?.let { network ->
                service.getNetworkCapabilities(network)
                    ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } ?: false
        }
    }

    private fun onNetworkStatusChangedInternal(event: Event.NetworkStatusChanged) {
        if (isNetworkAvailable != event.isAvailable) {
            alertManager?.showAlert(
                AlertData(
                    messageRes = if (event.isAvailable) R.string.internet_is_available else R.string.internet_is_unavailable,
                    alertBuilder = alertBuilder
                )
            )
        }
        isNetworkAvailable = event.isAvailable
        eventBusManager.post(event)
    }

    init {
        isNetworkAvailable = isNetworkAvailable2()
    }

    companion object {
        /**
         * Is true if a network is available
         */
        var isNetworkAvailable: Boolean = false
            private set

        private val STATUS_CHANGE_DELAY =
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) 2500L else 1000L
    }
}