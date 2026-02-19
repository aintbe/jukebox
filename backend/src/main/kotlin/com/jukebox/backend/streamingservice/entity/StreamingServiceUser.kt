package com.jukebox.backend.streamingservice.entity

import com.jukebox.backend.user.entity.User
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne

@Entity
class StreamingServiceUser(
    @EmbeddedId
    val pk: StreamingServiceUserPK,
    @MapsId("serviceId") // Specified in [StreamingServiceUserPK]
    @ManyToOne(fetch = FetchType.LAZY)
    val service: StreamingService,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: User,
)
