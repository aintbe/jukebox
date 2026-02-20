package com.jukebox.core.exception

import org.springframework.http.HttpStatusCode
import org.springframework.web.server.ResponseStatusException

open class BusinessException(
    statusCode: HttpStatusCode,
    val errorCode: String,
    reason: String,
) : ResponseStatusException(statusCode, reason) {
    override fun getReason(): String = super.reason ?: ""
}
