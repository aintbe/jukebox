package com.jukebox.api.common.advice

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class GlobalResponse<T>(
    val data: T? = null,
    val error: String? = null,
    val message: String? = null,
) {
    companion object {
        fun <T> success(data: T): GlobalResponse<T> =
            if (data is String) {
                GlobalResponse(message = data)
            } else {
                GlobalResponse(data = data)
            }

        fun error(
            code: String,
            message: String,
        ): GlobalResponse<Nothing> = GlobalResponse(error = code, message = message)
    }
}

typealias ErrorResponse = GlobalResponse<Nothing>
