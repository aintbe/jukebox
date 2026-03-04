package com.jukebox.core.dto

import com.jukebox.core.exception.BusinessException
import org.springframework.http.HttpStatusCode

data class BusinessExceptionDto(
    val statusValue: Int,
    val errorCode: String,
    val reason: String,
) {
    companion object {
        fun from(e: BusinessException): BusinessExceptionDto =
            BusinessExceptionDto(
                statusValue = e.statusCode.value(),
                errorCode = e.errorCode,
                reason = e.reason,
            )
    }

    fun toException(): BusinessException =
        BusinessException(
            statusCode = HttpStatusCode.valueOf(statusValue),
            errorCode = errorCode,
            reason = reason,
        )
}
