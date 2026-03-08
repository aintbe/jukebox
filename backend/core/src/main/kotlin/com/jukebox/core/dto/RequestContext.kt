package com.jukebox.core.dto

data class RequestContext(
    val jukeboxId: Long,
    val userId: Long,
    val streamingAccess: StreamingAccess,
)
