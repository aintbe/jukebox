package com.jukebox.relay.spotify

import com.jukebox.core.dto.RequestContext
import com.jukebox.core.dto.RequestDto
import com.jukebox.relay.common.web.CurrentRequestContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
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

    @PutMapping("/connect")
    suspend fun connect(
        @CurrentRequestContext context: RequestContext,
        @RequestBody request: RequestDto.Connect,
    ): ResponseEntity<Unit> {
        spotifyService.transferPlayback(context, request)
        return ResponseEntity.accepted().build()
    }
}
