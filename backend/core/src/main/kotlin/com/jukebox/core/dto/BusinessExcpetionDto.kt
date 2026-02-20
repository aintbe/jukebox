package com.jukebox.core.dto

import com.jukebox.core.exception.BusinessException
import org.springframework.http.HttpStatusCode

data class BusinessExcpetionDto(
    val statusValue: Int,
    val errorCode: String,
    val reason: String,
) {
    companion object {
        fun from(e: BusinessException): BusinessExcpetionDto =
            BusinessExcpetionDto(
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
