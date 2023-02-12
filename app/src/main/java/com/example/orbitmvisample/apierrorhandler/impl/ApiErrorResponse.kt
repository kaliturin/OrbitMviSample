package com.example.orbitmvisample.apierrorhandler.impl

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ApiErrorResponse(
    @field:JsonProperty("Error")
    val errors: List<ApiError>? = null
) : Parcelable {
    @Parcelize
    data class ApiError(
        @JsonProperty("Error")
        val error: String? = null,
        @JsonProperty("ErrorCode")
        val errorCode: String? = null,
        @JsonProperty("ErrorMessageText")
        val errorMessageText: String? = null
    ) : Parcelable
}