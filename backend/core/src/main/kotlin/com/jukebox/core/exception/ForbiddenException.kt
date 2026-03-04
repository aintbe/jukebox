package com.jukebox.core.exception

import org.springframework.http.HttpStatus

open class ForbiddenException(
    errorCode: String,
    reason: String,
) : BusinessException(HttpStatus.FORBIDDEN, errorCode, reason)

class StreamingServiceNotFoundException :
    ForbiddenException(
        errorCode = "STREAMING_SERVICE_NOT_FOUND",
        reason = "This account is not connected to any streaming service.",
    )

class StreamingServiceAuthRequiredException(
    serviceName: String,
) : ForbiddenException(
        errorCode = "STREAMING_SERVICE_AUTHENTICATION_REQUIRED",
        reason = "$serviceName requires re-authentication.",
    )
