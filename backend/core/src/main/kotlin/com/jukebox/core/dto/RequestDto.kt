package com.jukebox.core.dto

import jakarta.validation.constraints.NotBlank

class RequestDto {
    data class Connect(
        @field:NotBlank
        val deviceId: String,
    )
}
