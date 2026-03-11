package com.jukebox.api.jukebox.dto

import com.jukebox.core.dto.JukeboxSession

fun JukeboxInfo.Detail.toResponse() =
    JukeboxDto.GetResponse(
        id = id,
        handle = handle,
        serviceName = serviceName,
    )

fun JukeboxInfo.Detail.toCached(userId: Long?): JukeboxSession? =
    serviceName?.let { serviceName ->
        JukeboxSession(
            jukeboxId = id,
            serviceName = serviceName,
            hostId = hostId,
            userId = userId,
        )
    }
