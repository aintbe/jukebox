package com.jukebox.api.relay

import com.jukebox.core.dto.SharedDto
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/relay")
class RelayController(
    private val relayService: RelayService,
) {
    @PostMapping("/reissue")
    fun reissueStreamingAccess(request: SharedDto.ReissueAccessRequest) = relayService.reissueStreamingAccess(request)
}
