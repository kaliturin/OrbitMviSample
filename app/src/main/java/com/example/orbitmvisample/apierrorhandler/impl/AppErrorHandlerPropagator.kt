package com.example.orbitmvisample.apierrorhandler.impl

import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import com.example.orbitmvisample.apierrorhandler.AppErrorCode
import com.example.orbitmvisample.apierrorhandler.AppErrorHandler
import com.example.orbitmvisample.apierrorhandler.AppException
import com.example.orbitmvisample.fetcher.FetcherViewModel
import com.example.orbitmvisample.ui.alert.AlertData
import com.example.orbitmvisample.ui.alert.AlertManager
import timber.log.Timber

/**
 * Propagates specific types of handling exceptions
 */
class AppErrorHandlerPropagator(
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
            AppErrorCode.USER_IS_NOT_AUTHORIZED,
            AppErrorCode.SESSION_CLOSED,
            AppErrorCode.TECHNICAL_WORKS -> {
                // TODO: propagate exception
            }
            else -> {
            }
        }

        when (exception.errorCode) {
            AppErrorCode.USER_IS_NOT_AUTHORIZED, AppErrorCode.SESSION_CLOSED -> {
                // ignore the errors
            }
            else -> {
                if (!settingsHelper.getBoolean(SUPPRESS_ALERT)) {
                    // show alert message with the error message
                    val alertData = AlertData(
                        title = "Error", // TODO: strings.xml
                        message = exception.message,
                    )
                    try {
                        alertManager.showAlert(context, alertData)
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

        fun settings(suppressAlert: Boolean) = bundleOf(
            SUPPRESS_ALERT to suppressAlert
        )
    }
}

fun <T : Any> FetcherViewModel<T>.suppressAlerts(suppressAlert: Boolean = true) = apply {
    errorHandlerSettings(AppErrorHandlerPropagator.settings(suppressAlert))
}