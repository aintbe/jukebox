package com.jukebox.backend.auth.oauth2

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

/**
 * Replace [OAuth2AuthorizedClientService] to use Redis as datasource
 * for OAuth2 token managements.
 */
@Component
class RedisOAuth2AuthorizedClientService(
    private val oAuth2RedisTemplate: RedisTemplate<String, OAuth2AuthorizedClient>,
) : OAuth2AuthorizedClientService {
    private fun generateKey(principalName: String) = "oauth2_client:$principalName"

    override fun <T : OAuth2AuthorizedClient?> loadAuthorizedClient(
        clientRegistrationId: String?,
        principalName: String?,
    ): T? =
        principalName?.let {
            val client =
                oAuth2RedisTemplate.opsForValue().get(generateKey(it))
            @Suppress("UNCHECKED_CAST")
            return client as? T
        }

    override fun saveAuthorizedClient(
        authorizedClient: OAuth2AuthorizedClient?,
        principal: Authentication?,
    ) {
        if (authorizedClient == null || principal == null) return

        val key = generateKey(principal.name)
        oAuth2RedisTemplate.opsForValue().set(key, authorizedClient)
        oAuth2RedisTemplate.expire(key, 28, TimeUnit.DAYS)
    }

    override fun removeAuthorizedClient(
        clientRegistrationId: String?,
        principalName: String?,
    ) {
        principalName?.let {
            oAuth2RedisTemplate.delete(generateKey(it))
        }
    }
}
