package com.example.orbitmvisample.apierrorhandler.impl

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.annotation.WorkerThread
import com.example.orbitmvisample.BuildConfig
import com.example.orbitmvisample.R
import com.example.orbitmvisample.apierrorhandler.ApiErrorCode
import com.example.orbitmvisample.apierrorhandler.ApiErrorCode.*
import com.example.orbitmvisample.apierrorhandler.ApiException
import com.example.orbitmvisample.apierrorhandler.ApiExceptionBuilder
import com.example.orbitmvisample.apierrorhandler.impl.ApiExceptionBuilderImpl.ApiErrorMessage.*
import com.example.orbitmvisample.utils.JsonUtils
import com.fasterxml.jackson.databind.JsonMappingException
import retrofit2.HttpException
import timber.log.Timber
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

class ApiExceptionBuilderImpl(private val resources: Resources) : ApiExceptionBuilder {

    override suspend fun build(throwable: Throwable): ApiException {
        return when (throwable) {
            is ApiException -> throwable
            is HttpException -> parse(throwable)
            is SocketTimeoutException ->
                ApiException(CONNECTION_LOST, MSG_REQUEST_TIME_LIMIT.str, throwable)
            is SocketException, is UnknownHostException ->
                ApiException(CONNECTION_LOST, MSG_CONNECTION_LOST.str, throwable)
            is JsonMappingException ->
                ApiException(JSON_PARSING, MSG_SERVER_MSG_PARSING.str, throwable)
            is SSLException ->
                ApiException(SSL_EXCEPTION, MSG_CONNECTION_ERROR.str, throwable)
            else -> {
                if (BuildConfig.DEBUG)
                    ApiException(throwable.toString(), throwable)
                else // create user-friendly error message for production
                    ApiException(MSG_ERROR_HAPPENED.str, throwable)
            }
        }
    }

    @WorkerThread
    private fun parse(exception: HttpException): ApiException {
        val response = exception.response()

        if (response?.code() == GATEWAY_TIMEOUT_CODE)
            return ApiException(GATEWAY_TIMEOUT, MSG_GATEWAY_TIMEOUT.str)

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
                val code = ApiErrorCode.parse(error.errorCode ?: error.error)
                val message = error.errorMessageText ?: MSG_SERVICE_UNAVAILABLE.str
                ApiException(code, message)
            } else {
                // fail to parse an error
                ApiException(RESPONSE_PARSING_ERROR, MSG_SERVICE_UNAVAILABLE.str, cause)
            }
        } else {
            // error body is empty
            ApiException(RESPONSE_BODY_IS_EMPTY, MSG_RESPONSE_FORMAT.str)
        }
    }

    private enum class ApiErrorMessage(@StringRes private val stringRes: Int) {
        MSG_RESPONSE_FORMAT(R.string.error_response_format),
        MSG_SERVICE_UNAVAILABLE(R.string.error_service_unavailable),
        MSG_GATEWAY_TIMEOUT(R.string.error_gateway_timeout),
        MSG_REQUEST_TIME_LIMIT(R.string.error_request_time_limit),
        MSG_CONNECTION_LOST(R.string.error_connection_lost),
        MSG_SERVER_MSG_PARSING(R.string.error_server_msg_parsing),
        MSG_CONNECTION_ERROR(R.string.error_connection_error),
        MSG_ERROR_HAPPENED(R.string.error_happened);

        fun toString(resources: Resources): String {
            return resources.getString(stringRes)
        }
    }

    private val ApiErrorMessage.str get() = this.toString(resources)

    companion object {
        private const val GATEWAY_TIMEOUT_CODE = 504
    }
}