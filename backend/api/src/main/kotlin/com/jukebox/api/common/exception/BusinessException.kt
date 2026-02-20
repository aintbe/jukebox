package com.jukebox.api.common.exception

import org.springframework.http.HttpStatusCode
import org.springframework.web.server.ResponseStatusException

open class BusinessException(
    status: HttpStatusCode,
    val errorCode: String,
    reason: String,
) : ResponseStatusException(status, reason)
