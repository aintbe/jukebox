package com.jukebox.core.dto

data class JukeboxContext(
    val jukeboxId: Long,
    val userId: Long,
    val streamingAccess: StreamingAccess,
)
