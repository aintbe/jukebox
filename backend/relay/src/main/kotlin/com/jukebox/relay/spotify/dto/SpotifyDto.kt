package com.jukebox.relay.spotify.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.jukebox.core.dto.RequestDto

class SpotifyDto {
    data class ErrorResponse(
        val error: Error,
    ) {
        data class Error(
            val status: Int,
            val message: String,
        )
    }

    @JvmInline
    value class TransferRequest(
        private val request: RequestDto.Connect,
    ) {
        fun toBody() =
            mapOf(
                "device_ids" to listOf(request.deviceId),
                "play" to false,
            )
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PlaybackStateResponse(
        val device: Device,
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Device(
            val id: String,
            @JsonProperty("is_active")
            val isActive: Boolean,
            @JsonProperty("is_restricted")
            val isRestricted: Boolean,
            val type: String,
        )
    }
}
