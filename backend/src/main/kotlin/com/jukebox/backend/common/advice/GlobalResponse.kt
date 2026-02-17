package com.jukebox.backend.common.advice

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class GlobalResponse<T>(
    val data: T? = null,
    val error: String? = null,
    val message: String? = null,
) {
    companion object {
        fun <T> success(data: T): GlobalResponse<T> = GlobalResponse(data = data)

        fun success(message: String): GlobalResponse<Nothing> = GlobalResponse(message = message)

        fun error(
            code: String,
            message: String,
        ): GlobalResponse<Nothing> = GlobalResponse(error = code, message = message)
    }
}

typealias ErrorResponse = GlobalResponse<Nothing>
