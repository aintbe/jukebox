package com.jukebox.backend.user.dto

import org.springframework.security.oauth2.core.OAuth2AccessToken
import java.time.Instant
import kotlin.random.Random

class UserDto {
    class UserProfileResponse(
        profile: UserQueryDto.UserProfile,
        accessToken: OAuth2AccessToken?,
    ) {
        val id = profile.id
        val username = profile.username
        val streamingServiceAccess =
            profile.streamingService?.let { serviceName ->
                accessToken?.run {
                    StreamingServiceAccess(
                        serviceName,
                        tokenValue,
                        expiresAt,
                    )
                }
            }
    }
}

data class StreamingServiceAccess(
    val serviceName: String,
    val token: String,
    val expiresAt: Instant?,
) {
    val a: Int = Random.nextInt()
}
