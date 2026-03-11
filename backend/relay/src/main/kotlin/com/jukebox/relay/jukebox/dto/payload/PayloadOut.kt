package com.jukebox.relay.jukebox.dto.payload

/**
 * Outbound payloads do not need to be specified as subtypes of
 * [Payload] since backend code does not deserialize them.
 */
object PayloadOut {
    const val HOST_HAS_LEFT = "HOST_HAS_LEFT"

    data class GuestChange(
        val userId: String,
        val isJoining: Boolean,
    ) : Payload("GUEST_CHANGE")

    class HostHasLeft : Payload("HOST_HAS_LEFT")

    sealed class Error(
        val errorCode: String,
    ) : Payload("ERROR")

    class JukeboxUnavailableError : Error("JUKEBOX_UNAVAILABLE")

    class BadRequestError : Error("BAD_REQUEST")

    class UnknownError : Error("UNKNOWN")

    class StreamingServiceAuthRequiredError : Error("STREAMING_SERIVCE_AUTH_REQUIRED")
}
