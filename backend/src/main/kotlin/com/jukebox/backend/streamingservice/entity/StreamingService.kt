package com.jukebox.backend.streamingservice.entity

import com.nimbusds.jose.shaded.jcip.Immutable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.PreRemove
import jakarta.persistence.Table

@Entity
@Immutable
@Table(name = "streaming_service")
class StreamingService(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,
    @Column(nullable = false, unique = true)
    val name: String,
) {
    @PrePersist
    fun prePersist(): Unit = throw IllegalStateException("Cannot add ${javaClass.simpleName}.")

    @PreRemove
    fun preRemove(): Unit = throw IllegalStateException("Cannot remove ${javaClass.simpleName}.")
}
