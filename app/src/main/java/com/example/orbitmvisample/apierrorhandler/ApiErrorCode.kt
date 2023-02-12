package com.example.orbitmvisample.apierrorhandler

enum class ApiErrorCode(val code: String) {
    UNKNOWN("Unknown"),
    RESPONSE_BODY_IS_EMPTY("ResponseBodyIsEmpty"),
    RESPONSE_PARSING_ERROR("ResponseParsingError"),
    CONNECTION_LOST("ConnectionLost"),
    SSL_EXCEPTION("SSLException"),
    JSON_PARSING("JsonParsing"),
    GATEWAY_TIMEOUT("504"),
    USER_IS_NOT_AUTHORIZED("444"), // TODO
    SESSION_CLOSED("SessionClosed"),
    TECHNICAL_WORKS("InaccessibilityError"),
    ;

    override fun toString(): String = code

    companion object {
        fun parse(code: String?): ApiErrorCode {
            return values().find { it.code == code } ?: UNKNOWN
        }
    }
}