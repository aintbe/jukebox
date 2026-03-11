package com.jukebox.relay.jukebox.dto

import com.jukebox.core.dto.JukeboxSession

data class WebsocketMessage(
    val filter: (receiver: JukeboxSession) -> Boolean,
    val event: String,
    val payload: String,
)
