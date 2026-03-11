package com.jukebox.relay.jukebox.dto

import com.jukebox.core.dto.JukeboxSession
import com.jukebox.core.dto.StreamingAccess

fun JukeboxSession.toAuthSession() =
    userId?.let { userId ->
        AuthSession(
            jukeboxId = jukeboxId,
            serviceName = serviceName,
            hostId = hostId,
            userId = userId,
        )
    }

fun JukeboxInfo.CachedStreamingAccess.toStreamingAccess() =
    StreamingAccess(
        serviceName = clientRegistration.registrationId,
        token = accessToken.tokenValue,
        expiresAt = accessToken.expiresAt,
    )
