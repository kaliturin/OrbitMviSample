package com.example.orbitmvisample.apierrorhandler.impl

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.core.os.bundleOf
import com.example.orbitmvisample.apierrorhandler.AppErrorCode
import com.example.orbitmvisample.apierrorhandler.AppErrorHandler
import com.example.orbitmvisample.apierrorhandler.AppException

/**
 * Propagates specific types of handling exceptions
 */
class AppErrorHandlerPropagator(
    private val context: Context,
    defSettings: Bundle = Bundle()
) : AppErrorHandler {

    private val settingsHelper = AppErrorHandlerSettingsHelper(defSettings)

    override suspend fun handle(throwable: Throwable, settings: Bundle?): AppException {
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
            }
            else -> {
                if (!settingsHelper.getBoolean(SUPPRESS_ALERT)) {
                    //TODO: show alert
                    // Alert.error().message(exception.message).show()
                    Toast.makeText(context, exception.message, Toast.LENGTH_LONG).show()
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