package com.jukebox.core.exception

import org.springframework.http.HttpStatus

open class ConflictException(
    errorCode: String,
    reason: String,
) : BusinessException(HttpStatus.CONFLICT, errorCode, reason)
