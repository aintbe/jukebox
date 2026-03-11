package com.jukebox.relay.jukebox.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.Instant

class JukeboxInfo {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class CachedStreamingAccess(
        val clientRegistration: ClientRegistration,
        val accessToken: AccessToken,
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class ClientRegistration(
            val registrationId: String,
        )

        @JsonIgnoreProperties(ignoreUnknown = true)
        data class AccessToken(
            val tokenValue: String,
            val expiresAt: Instant?,
        )
    }
}
