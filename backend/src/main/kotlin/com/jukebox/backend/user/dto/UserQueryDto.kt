package com.jukebox.backend.user.dto

class UserQueryDto {
    data class UserProfile(
        val id: Long,
        val username: String,
        val streamingService: String?,
    )
}
