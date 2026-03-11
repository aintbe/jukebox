package com.jukebox.api.jukebox

import com.jukebox.api.auth.dto.AuthUser
import com.jukebox.api.common.cache.CacheService
import com.jukebox.api.jukebox.dto.JukeboxDto
import com.jukebox.api.jukebox.dto.toResponse
import com.jukebox.api.streamingservice.StreamingServiceRepository
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/jukebox/{handle}")
@PreAuthorize("permitAll()")
class JukeboxController(
    private val jukeboxService: JukeboxService,
    private val streamingServiceRepository: StreamingServiceRepository,
    private val cacheService: CacheService,
) {
    @GetMapping()
    fun getJukebox(
        @PathVariable handle: String,
    ) = jukeboxService.getJukebox(handle).toResponse()

    @PostMapping("/join")
    fun joinJukebox(
        @PathVariable handle: String,
        @AuthenticationPrincipal user: AuthUser?,
    ) = jukeboxService
        .joinJukebox(handle, user)
        .let(JukeboxDto::JoinResponse)
}
