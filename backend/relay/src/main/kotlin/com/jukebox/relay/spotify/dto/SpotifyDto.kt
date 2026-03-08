package com.jukebox.relay.spotify.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

class SpotifyDto {
    data class ErrorResponse(
        val error: Error,
    ) {
        data class Error(
            val status: Int,
            val message: String,
        )
    }

    data class DeviceResponse(
        val devices: List<Device>,
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

    data class ConnectionResponse(
        val deviceId: String?,
    )

    data class TransferRequest(
        @field:NotBlank
        val deviceId: String,
    ) {
        fun toBody() =
            mapOf(
                "device_ids" to listOf(deviceId),
                "play" to false,
            )
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PlaybackStateResponse(
        val device: DeviceResponse.Device,
    )
}
