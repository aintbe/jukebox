package com.jukebox.core.dto

open class JukeboxSession(
    val jukeboxId: Long,
    val serviceName: String,
    val hostId: Long,
    open val userId: Long?,
)
