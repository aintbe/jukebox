package com.jukebox.core.exception

import org.springframework.http.HttpStatus

class InternalException :
    BusinessException(
        statusCode = HttpStatus.INTERNAL_SERVER_ERROR,
        errorCode = "INTERNAL_SERVER_ERROR",
        reason = "Server encountered an unexpected error.",
    )

class ExternalException(
    serverLabel: String,
    reason: String? = null,
) : BusinessException(
        statusCode = HttpStatus.INTERNAL_SERVER_ERROR,
        errorCode = "${serverLabel.uppercase()}_SERVER_ERROR",
        reason =
            reason
                ?: "$serverLabel encountered an unexpected error. Please try again later.",
    )
