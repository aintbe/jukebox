package com.jukebox.relay.jukebox.dto.payload

/**
 * All inbound payloads must be specified as subtypes of [Payload] in order
 * to be handled by the [com.jukebox.relay.jukebox.JukeboxWebsocketController].
 */
object PayloadIn {
    internal const val SPOTIFY_TRANSFER_DEVICE = "SPOTIFY_TRANSFER_DEVICE"

    data class SpotifyTransfer(
        val deviceId: String,
    ) : Payload(SPOTIFY_TRANSFER_DEVICE)
}
