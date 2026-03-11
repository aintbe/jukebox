package com.jukebox.relay.jukebox.dto.payload

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "event",
    visible = true,
)
@JsonSubTypes(
    JsonSubTypes.Type(value = PayloadIn.SpotifyTransfer::class, name = PayloadIn.SPOTIFY_TRANSFER_DEVICE),
    JsonSubTypes.Type(value = PayloadDual.TogglePlay::class, name = PayloadDual.TOGGLE_PLAY),
)
open class Payload(
    val event: String,
)
