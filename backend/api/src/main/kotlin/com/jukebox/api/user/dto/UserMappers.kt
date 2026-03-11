package com.jukebox.api.user.dto

import com.jukebox.core.dto.StreamingAccess
import org.springframework.security.oauth2.core.OAuth2AccessToken

fun UserInfo.UserProfile.toResponse(accessToken: OAuth2AccessToken?) =
    UserDto.GetProfileResponse(
        id = id,
        username = username,
        jukebox = jukebox,
        streamingAccess =
            streamingServiceName?.let { serviceName ->
                accessToken?.run {
                    StreamingAccess(
                        serviceName,
                        tokenValue,
                        expiresAt,
                    )
                }
            },
    )
