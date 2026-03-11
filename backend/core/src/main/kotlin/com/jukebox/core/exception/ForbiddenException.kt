package com.jukebox.core.exception

import org.springframework.http.HttpStatus

open class ForbiddenException(
    errorCode: String,
    reason: String,
) : BusinessException(HttpStatus.FORBIDDEN, errorCode, reason)

class StreamingServiceAuthRequiredException(
    userId: Long,
) : ForbiddenException(
        errorCode = "STREAMING_SERVICE_AUTHENTICATION_REQUIRED",
        reason = "User $userId requires re-authentication.",
    )
