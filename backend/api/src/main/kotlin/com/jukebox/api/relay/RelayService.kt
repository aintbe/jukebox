package com.jukebox.api.relay

import com.jukebox.core.dto.SharedDto
import com.jukebox.core.dto.StreamingAccess
import com.jukebox.core.exception.StreamingServiceAuthRequiredException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.stereotype.Service
import java.security.Principal

@Service
class RelayService(
    private val streamingManager: OAuth2AuthorizedClientManager,
) {
    fun reissueStreamingAccess(request: SharedDto.ReissueAccessRequest): StreamingAccess {
        val authentication =
            UsernamePasswordAuthenticationToken(
                object : Principal {
                    override fun getName(): String? = request.userId.toString()
                },
                null,
                listOf(),
            )

        // Below works as same as [RegisteredOAuth2AuthorizedClient] annotation,
        // which means Spring automatically reissues access token if it's expired.
        val authorizeRequest =
            OAuth2AuthorizeRequest
                .withClientRegistrationId(request.serviceName)
                .principal(authentication)
                .build()
        val authorizedClient =
            streamingManager.authorize(authorizeRequest)
                ?: throw StreamingServiceAuthRequiredException(request.userId)
        val accessToken = authorizedClient.accessToken

        return StreamingAccess(
            serviceName = request.serviceName,
            token = accessToken.tokenValue,
            expiresAt = accessToken.expiresAt,
        )
    }
}
