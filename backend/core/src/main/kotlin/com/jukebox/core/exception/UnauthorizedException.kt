package com.jukebox.core.exception

import org.springframework.http.HttpStatus

class UnauthorizedException(
    errorCode: String,
    reason: String,
) : BusinessException(HttpStatus.UNAUTHORIZED, errorCode, reason)
