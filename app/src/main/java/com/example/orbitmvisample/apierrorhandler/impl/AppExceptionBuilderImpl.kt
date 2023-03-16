package com.example.orbitmvisample.apierrorhandler.impl

import androidx.annotation.StringRes
import androidx.annotation.WorkerThread
import com.example.orbitmvisample.BuildConfig
import com.example.orbitmvisample.R
import com.example.orbitmvisample.apierrorhandler.AppErrorCode
import com.example.orbitmvisample.apierrorhandler.AppErrorCode.*
import com.example.orbitmvisample.apierrorhandler.AppException
import com.example.orbitmvisample.apierrorhandler.AppExceptionBuilder
import com.example.orbitmvisample.apierrorhandler.impl.AppExceptionBuilderImpl.AppErrorMessage.*
import com.example.orbitmvisample.utils.JsonUtils
import com.example.orbitmvisample.utils.Resources
import com.fasterxml.jackson.databind.JsonMappingException
import retrofit2.HttpException
import timber.log.Timber
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

class AppExceptionBuilderImpl : AppExceptionBuilder {

    override suspend fun build(throwable: Throwable): AppException {
        return when (throwable) {
            is AppException -> throwable
            is HttpException -> parse(throwable)
            is SocketTimeoutException ->
                AppException(CONNECTION_LOST, MSG_REQUEST_TIME_LIMIT.str, throwable)
            is SocketException, is UnknownHostException ->
                AppException(CONNECTION_LOST, MSG_CONNECTION_LOST.str, throwable)
            is JsonMappingException ->
                AppException(JSON_PARSING, MSG_SERVER_MSG_PARSING.str, throwable)
            is SSLException ->
                AppException(SSL_EXCEPTION, MSG_CONNECTION_ERROR.str, throwable)
            else -> {
                if (BuildConfig.DEBUG)
                    AppException(throwable.toString(), throwable)
                else // create user-friendly error message for production
                    AppException(MSG_ERROR_HAPPENED.str, throwable)
            }
        }
    }

    @WorkerThread
    private fun parse(exception: HttpException): AppException {
        val response = exception.response()

        if (response?.code() == GATEWAY_TIMEOUT_CODE)
            return AppException(GATEWAY_TIMEOUT, MSG_GATEWAY_TIMEOUT.str)

        val errorBody = response?.errorBody()
        return if (errorBody != null) {
            var cause: Throwable? = null
            val apiResponseError = try {
                JsonUtils.fromJson(errorBody.source().readUtf8(), ApiErrorResponse::class)
            } catch (e: Exception) {
                Timber.e(e)
                cause = e
                null
            }
            val error = apiResponseError?.errors?.lastOrNull()
            if (error != null) {
                // the error parsed successfully
                val code = AppErrorCode.parse(error.errorCode ?: error.error)
                val message = error.errorMessageText ?: MSG_SERVICE_UNAVAILABLE.str
                AppException(code, message)
            } else {
                // fail to parse an error
                AppException(RESPONSE_PARSING_ERROR, MSG_SERVICE_UNAVAILABLE.str, cause)
            }
        } else {
            // error body is empty
            AppException(RESPONSE_BODY_IS_EMPTY, MSG_RESPONSE_FORMAT.str)
        }
    }

    private enum class AppErrorMessage(@StringRes private val stringRes: Int) {
        MSG_RESPONSE_FORMAT(R.string.error_response_format),
        MSG_SERVICE_UNAVAILABLE(R.string.error_service_unavailable),
        MSG_GATEWAY_TIMEOUT(R.string.error_gateway_timeout),
        MSG_REQUEST_TIME_LIMIT(R.string.error_request_time_limit),
        MSG_CONNECTION_LOST(R.string.error_connection_lost),
        MSG_SERVER_MSG_PARSING(R.string.error_server_msg_parsing),
        MSG_CONNECTION_ERROR(R.string.error_connection_error),
        MSG_ERROR_HAPPENED(R.string.error_happened);

        override fun toString(): String {
            return Resources.getString(stringRes)
        }
    }

    private val AppErrorMessage.str get() = this.toString()

    companion object {
        private const val GATEWAY_TIMEOUT_CODE = 504
    }
}