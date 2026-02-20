package com.jukebox.core.exception

import org.springframework.http.HttpStatus

class NotFoundException(
    errorCode: String,
    reason: String,
) : BusinessException(HttpStatus.NOT_FOUND, errorCode, reason)
