package com.jukebox.core.exception

import org.springframework.http.HttpStatus

open class UnauthenticatedException(
    errorCode: String,
    reason: String,
) : BusinessException(HttpStatus.UNAUTHORIZED, errorCode, reason)

class UserNotFoundException(
    id: Long?,
) : UnauthenticatedException(
        errorCode = "USER_NOT_FOUND",
        reason = "Could not find user (id=$id)",
    )
