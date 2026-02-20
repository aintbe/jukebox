package com.jukebox.core.exception

import org.springframework.http.HttpStatus

class InternalException :
    BusinessException(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "INTERNAL_SERVER_ERROR",
        "Server encountered an unexpected error.",
    )
