package com.jukebox.api.jukebox

import com.jukebox.api.jukebox.web.CurrentRequestContext
import com.jukebox.core.dto.RequestContext
import com.jukebox.core.dto.RequestDto
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/jukebox/{jukeboxId}/host")
@PreAuthorize("isAuthenticated()")
class JukeboxHostController(
    private val jukeboxService: JukeboxService,
) {
    @PutMapping("/connect")
    fun connect(
        @CurrentRequestContext context: RequestContext,
        @Valid @RequestBody request: RequestDto.Connect,
    ): ResponseEntity<Unit> {
        jukeboxService.connect(context, request)
        return ResponseEntity.accepted().build()
    }
}
