package com.jukebox.core.dto

class SharedDto {
    data class ReissueAccessRequest(
        val serviceName: String,
        val userId: Long,
    )
}
