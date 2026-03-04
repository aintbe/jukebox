package com.jukebox.relay.spotify

import com.jukebox.core.dto.RequestContext
import com.jukebox.core.dto.RequestDto
import com.jukebox.core.exception.ExternalException
import com.jukebox.core.properties.EndpointProperties
import com.jukebox.relay.common.web.StreamingClientService
import com.jukebox.relay.spotify.dto.SpotifyDto
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.awaitEntity
import reactor.core.publisher.Mono

@Service
class SpotifyService(
    endpointProperties: EndpointProperties,
) : StreamingClientService(serviceConfig = endpointProperties.spotify) {
    override fun extractErrorMessage(response: ClientResponse): Mono<String> =
        response
            .bodyToMono(SpotifyDto.ErrorResponse::class.java)
            .map { it.error.message }

    /**
     * [Spotify API] Transfer playback to a new device and optionally begin playback.
     *
     * cf. https://developer.spotify.com/documentation/web-api/reference/transfer-a-users-playback
     */
    suspend fun transferPlayback(
        context: RequestContext,
        request: RequestDto.Connect,
    ) {
        client
            .request(
                method = HttpMethod.PUT,
                uri = "/me/player",
                token = context.streamingAccess.token,
                body = SpotifyDto.TransferRequest(request).toBody(),
            ).onStatus({ it.isSameCodeAs(HttpStatus.NOT_FOUND) }) {
                log.error("Spotify returned invalid device id. (jukeboxId=${context.jukeboxId})")
                Mono.error(ExternalException(serviceLabel))
            }.toBodilessEntity() // no need to check response body
            .awaitSingle()
    }

    /**
     * [Spotify API] Get information about the user’s current playback state, including track or episode, progress, and active device.
     *
     * cf. https://developer.spotify.com/documentation/web-api/reference/get-information-about-the-users-current-playback
     */
    private suspend fun getPlaybackState(context: RequestContext): ResponseEntity<SpotifyDto.PlaybackStateResponse> =
        client
            .request(
                method = HttpMethod.GET,
                uri = "/me/player",
                token = context.streamingAccess.token,
            ).awaitEntity<SpotifyDto.PlaybackStateResponse>()
}
