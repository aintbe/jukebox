package com.jukebox.api.streamingservice.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class StreamingServiceUserPK(
    @Column(name = "streaming_service_id", nullable = false)
    val serviceId: Int,
    @Column(name = "streaming_service_user_id", length = 100)
    val serviceUserId: String,
) : Serializable
