package com.jukebox.relay.jukebox.dto.payload

interface PlaybackPayload

/**
 * All dual payloads must be specified as subtypes of [Payload] in order
 * to be handled by the [com.jukebox.relay.jukebox.JukeboxWebsocketController].
 */
object PayloadDual {
    const val TOGGLE_PLAY = "TOGGLE_PLAY"

    class TogglePlay(
        val isPlaying: Boolean,
    ) : Payload(TOGGLE_PLAY),
        PlaybackPayload
}
