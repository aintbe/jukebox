package com.jukebox.core.exception

import org.springframework.http.HttpStatus

open class BadRequestException(
    errorCode: String,
    reason: String,
) : BusinessException(HttpStatus.BAD_REQUEST, errorCode, reason)

class BindingException(
    reason: String,
) : BadRequestException("INVALID_SYNTAX", reason)
