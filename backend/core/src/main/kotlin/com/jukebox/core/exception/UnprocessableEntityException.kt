package com.jukebox.core.exception

import org.springframework.http.HttpStatus

open class UnprocessableEntityException(
    errorCode: String,
    reason: String,
) : BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, errorCode, reason)

class JukeboxUnavailableException :
    UnprocessableEntityException(
        "JUKEBOX_UNAVAILABLE",
        "This jukebox is currently unavailable. Please reach out to the host for access.",
    )
