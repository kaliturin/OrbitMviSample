package com.example.orbitmvisample.apierrorhandler.impl

import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import com.example.orbitmvisample.R
import com.example.orbitmvisample.apierrorhandler.AppErrorCode
import com.example.orbitmvisample.apierrorhandler.AppErrorHandler
import com.example.orbitmvisample.apierrorhandler.AppException
import com.example.orbitmvisample.eventbus.Event
import com.example.orbitmvisample.eventbus.EventBusManager
import com.example.orbitmvisample.fetcher.FetcherViewModel
import com.example.orbitmvisample.ui.alert.AlertData
import com.example.orbitmvisample.ui.alert.AlertManager
import timber.log.Timber

/**
 * Propagates specific types of handling exceptions
 */
class AppErrorHandlerPropagator(
    private val eventBusManager: EventBusManager,
    private val alertManager: AlertManager,
    defSettings: Bundle = Bundle()
) : AppErrorHandler {

    private val settingsHelper = AppErrorHandlerSettingsHelper(defSettings)

    override suspend fun handle(
        throwable: Throwable, context: Context?, settings: Bundle?
    ): AppException {
        settingsHelper.setCurrentSettings(settings)

        val exception = throwable as? AppException
            ?: throw IllegalArgumentException(
                "${AppException::class.qualifiedName} as argument is expecting"
            )

        when (exception.errorCode) {
            AppErrorCode.UNKNOWN -> {
                // TODO: track exception
            }
            AppErrorCode.USER_IS_NOT_AUTHORIZED, AppErrorCode.SESSION_CLOSED -> {
                eventBusManager.post(Event.UserNotAuthorized(), true)
            }
            AppErrorCode.TECHNICAL_WORKS -> {
                eventBusManager.post(Event.TechnicalWorks(), true)
            }
            else -> {
            }
        }

        when (exception.errorCode) {
            AppErrorCode.USER_IS_NOT_AUTHORIZED, AppErrorCode.SESSION_CLOSED -> {
                // ignore the errors
            }
            else -> {
                if (context != null && !settingsHelper.getBoolean(SUPPRESS_ALERT)) {
                    try {
                        // show the alert with the error message
                        alertManager.showAlert(
                            context, AlertData(
                                title = context.getString(R.string.error_title),
                                message = exception.message,
                                // this means that we don't want to open the same alert more than ones in the period
                                repeatingMills = ALERT_REPEATING_MILLS
                            )
                        )
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                }
            }
        }
        return exception
    }

    companion object {
        private const val SUPPRESS_ALERT = "AppErrorHandlerPropagator.SUPPRESS_ALERT"
        private const val ALERT_REPEATING_MILLS = 1000L

        fun settings(suppressAlert: Boolean) = bundleOf(
            SUPPRESS_ALERT to suppressAlert
        )
    }
}

fun <T : Any> FetcherViewModel<T>.suppressAlerts(suppressAlert: Boolean = true) = apply {
    errorHandlerSettings(AppErrorHandlerPropagator.settings(suppressAlert))
}