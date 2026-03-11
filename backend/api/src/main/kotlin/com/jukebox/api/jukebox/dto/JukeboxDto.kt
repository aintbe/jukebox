package com.jukebox.api.jukebox.dto

class JukeboxDto {
    data class GetResponse(
        val id: Long,
        val handle: String,
        val serviceName: String?,
    )

    data class JoinResponse(
        val ticket: String,
    )
}
