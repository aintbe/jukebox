package com.jukebox.relay.spotify

import com.jukebox.core.dto.RequestContext
import com.jukebox.relay.common.web.CurrentRequestContext
import com.jukebox.relay.spotify.dto.SpotifyDto
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/spotify")
class SpotifyController(
    private val spotifyService: SpotifyService,
) {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("/connection")
    suspend fun getActiveConnection(
        @CurrentRequestContext context: RequestContext,
    ): ResponseEntity<SpotifyDto.ConnectionResponse> {
        val deviceId = spotifyService.getActiveDeviceId(context)
        return ResponseEntity.ok(
            SpotifyDto.ConnectionResponse(deviceId),
        )
    }

    @PutMapping("/connect")
    suspend fun connect(
        @CurrentRequestContext context: RequestContext,
        @Valid @RequestBody request: SpotifyDto.TransferRequest,
    ): ResponseEntity<Unit> {
        spotifyService.transferPlayback(context, request)
        return ResponseEntity.accepted().build()
    }
}
