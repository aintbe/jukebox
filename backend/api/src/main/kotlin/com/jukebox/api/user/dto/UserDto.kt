package com.jukebox.api.user.dto

import com.jukebox.core.dto.StreamingAccess

class UserDto {
    class GetProfileResponse(
        val id: Long,
        val username: String,
        val jukebox: UserInfo.Jukebox?,
        val streamingAccess: StreamingAccess?,
    )
}
