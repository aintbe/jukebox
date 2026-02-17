package com.jukebox.backend.oauth2.entity

import OAuth2Id
import com.jukebox.backend.user.entity.User
import com.nimbusds.jose.shaded.jcip.Immutable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne

@Entity
// Records in this table cannot be updated
@Immutable
class OAuth2(
    @EmbeddedId
    val id: OAuth2Id,
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
)
