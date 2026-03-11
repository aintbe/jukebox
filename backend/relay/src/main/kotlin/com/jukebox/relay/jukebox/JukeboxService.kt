package com.jukebox.relay.jukebox

import com.jukebox.core.dto.StreamingAccess
import com.jukebox.relay.common.cache.Cache
import com.jukebox.relay.common.cache.CacheService
import com.jukebox.relay.jukebox.dto.AuthSession
import com.jukebox.relay.jukebox.dto.payload.PayloadIn
import com.jukebox.relay.jukebox.dto.payload.PayloadOut
import com.jukebox.relay.jukebox.dto.toStreamingAccess
import com.jukebox.relay.remote.ApiService
import com.jukebox.relay.remote.SpotifyService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import java.time.Instant

@Service
class JukeboxService(
    private val objectMapper: ObjectMapper,
    private val messagePublisher: MessagePublisher,
    private val cacheService: CacheService,
    private val apiService: ApiService,
    private val spotifyService: SpotifyService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun getStreamingAccess(session: AuthSession): StreamingAccess? {
        val cached = cacheService.get(Cache.OAuth2Client(session.hostId))?.toStreamingAccess()
        if (cached != null) {
            val cacheExpiresAt = cached.expiresAt
            if (cacheExpiresAt == null || cacheExpiresAt >= Instant.now()) return cached

            // Cached streaming access has been expired; request reissuing to the api server.
            val reissued = apiService.reissueStreamingAccess(cached.serviceName, session.hostId)
            if (reissued != null) return reissued
        }

        messagePublisher.broadcast(session, PayloadOut.StreamingServiceAuthRequiredError())
        return null
    }

    suspend fun transferSpotifyDevice(
        session: AuthSession,
        request: PayloadIn.SpotifyTransfer,
    ) {
        val access = getStreamingAccess(session) ?: return
        spotifyService.transferPlayback(session.jukeboxId, access.token, request.deviceId)
    }

//    suspend fun togglePlay(
//        session: JukeboxSession,
//        payload: PayloadInNOut.TogglePlay,
//    ) {
//        if (session.userId == session.hostId) {
//            eventPublisher.sendToOthers(session, payload)
//        }
//    }
}
