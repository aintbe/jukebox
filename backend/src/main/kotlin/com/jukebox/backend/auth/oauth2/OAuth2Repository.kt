package com.jukebox.backend.auth.oauth2

import OAuth2Id
import com.jukebox.backend.oauth2.entity.OAuth2
import com.jukebox.backend.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface OAuth2Repository : JpaRepository<OAuth2, OAuth2Id> {
    @Query(
        """
        SELECT o.user
        FROM OAuth2 o
        WHERE o.id.provider = :provider
          AND o.id.providerId = :providerId
    """,
    )
    fun findUserById(
        provider: String,
        providerId: String,
    ): User?
}
