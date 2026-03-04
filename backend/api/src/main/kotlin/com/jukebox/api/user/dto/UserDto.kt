package com.jukebox.api.user.dto

import com.jukebox.core.dto.StreamingAccess
import org.springframework.security.oauth2.core.OAuth2AccessToken

class UserDto {
    class UserProfileResponse(
        profile: UserQueryDto.UserProfile,
        accessToken: OAuth2AccessToken?,
    ) {
        val id = profile.id
        val username = profile.username
        val streamingAccess =
            profile.streamingServiceName?.let { serviceName ->
                accessToken?.run {
                    StreamingAccess(
                        serviceName,
                        tokenValue,
                        expiresAt,
                    )
                }
            }
        val jukebox = profile.jukebox
    }
}
