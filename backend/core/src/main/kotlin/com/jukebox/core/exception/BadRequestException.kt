package com.jukebox.core.exception

import org.springframework.http.HttpStatus

class BadRequestException(
    errorCode: String,
    reason: String,
) : BusinessException(HttpStatus.BAD_REQUEST, errorCode, reason)
