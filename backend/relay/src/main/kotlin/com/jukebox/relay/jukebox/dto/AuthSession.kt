package com.jukebox.relay.jukebox.dto

import com.jukebox.core.dto.JukeboxSession

class AuthSession(
    jukeboxId: Long,
    serviceName: String,
    hostId: Long,
    override val userId: Long,
) : JukeboxSession(jukeboxId, serviceName, hostId, userId)
