package com.jukebox.core.dto

import java.time.Instant

data class StreamingAccess(
    val serviceName: String,
    val token: String,
    val expiresAt: Instant?,
)
