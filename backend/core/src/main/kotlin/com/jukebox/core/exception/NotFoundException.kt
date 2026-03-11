package com.jukebox.core.exception

import org.springframework.http.HttpStatus

open class NotFoundException(
    errorCode: String,
    reason: String,
) : BusinessException(HttpStatus.NOT_FOUND, errorCode, reason)

class EntityNotFoundException(
    entity: String,
    id: Any,
) : NotFoundException(
        "${entity.uppercase()}_NOT_FOUND",
        "Could not find entity $entity by $id",
    )
