package com.jukebox.relay.remote

import com.jukebox.core.dto.RequestContext
import com.jukebox.core.exception.ExternalException
import com.jukebox.core.properties.EndpointProperties
import com.jukebox.relay.remote.dto.SpotifyInfo
import com.jukebox.relay.remote.web.RemoteClientService
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import org.springframework.web.reactive.function.client.awaitEntity
import reactor.core.publisher.Mono

@Service
class SpotifyService(
    endpointProperties: EndpointProperties,
) : RemoteClientService(config = endpointProperties.spotify) {
    override fun extractErrorMessage(response: ClientResponse): Mono<String> =
        response
            .bodyToMono(SpotifyInfo.ErrorResponse::class.java)
            .map { it.error.message }

    /**
     * [Spotify Web API] Get information about the user’s current playback state, including track or episode, progress, and active device.
     *
     * cf. https://developer.spotify.com/documentation/web-api/reference/get-information-about-the-users-current-playback
     */
    private suspend fun getPlaybackState(context: RequestContext): ResponseEntity<SpotifyInfo.PlaybackStateResponse> =
        client
            .request(
                method = HttpMethod.GET,
                uri = "/me/player",
                token = context.streamingAccess.token,
            ).awaitEntity<SpotifyInfo.PlaybackStateResponse>()

    /**
     * [Spotify Web API] Transfer playback to a new device and optionally begin playback.
     *
     * cf. https://developer.spotify.com/documentation/web-api/reference/transfer-a-users-playback
     */
    suspend fun transferPlayback(
        jukeboxId: Long,
        token: String,
        deviceId: String,
    ) {
        val body = mapOf("device_ids" to listOf(deviceId), "play" to false)
        client
            .request(
                method = HttpMethod.PUT,
                uri = "/me/player",
                token = token,
                body = body,
            ).onStatus({ it.isSameCodeAs(HttpStatus.NOT_FOUND) }) {
                log.error("Spotify returned invalid device id $deviceId. (jukeboxId=$jukeboxId)")
                Mono.error(ExternalException(remoteServerLabel))
            }.toBodilessEntity() // no need to check response body
            .awaitSingle()
    }

    /**
     * [Spotify Web API]
     * cf. https://developer.spotify.com/documentation/web-api/reference/get-a-users-available-devices
     */
    suspend fun getActiveDeviceId(context: RequestContext): String? {
        val response =
            client
                .request(
                    method = HttpMethod.GET,
                    uri = "/me/player/devices",
                    token = context.streamingAccess.token,
                ).awaitBodyOrNull<SpotifyInfo.DeviceResponse>()

        return response?.devices?.find { it.isActive }?.id
    }
}
