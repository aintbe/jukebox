package com.jukebox.core.exception

import org.springframework.http.HttpStatus

class InternalException :
    BusinessException(
        statusCode = HttpStatus.INTERNAL_SERVER_ERROR,
        errorCode = "INTERNAL_SERVER_ERROR",
        reason = "Server encountered an unexpected error.",
    )

class ExternalException(
    serviceLabel: String,
    reason: String? = null,
) : BusinessException(
        statusCode = HttpStatus.INTERNAL_SERVER_ERROR,
        errorCode = "${serviceLabel.uppercase()}_SERVER_ERROR",
        reason =
            reason
                ?: "$serviceLabel encountered an unexpected error. Please try again later.",
    )
