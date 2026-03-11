package com.jukebox.api.auth.dto

import com.jukebox.api.user.entity.User
import io.jsonwebtoken.Claims
import java.security.Principal

data class AuthUser(
    val userId: Long,
    val username: String,
    val authorities: List<UserRole>,
) : Principal {
    override fun getName(): String? = userId.toString()

    companion object {
        fun from(user: User): AuthUser =
            AuthUser(
                userId = user.savedId,
                username = user.username,
                // TODO: add actual roles
                authorities = listOf(UserRole.TODO),
            )

        fun from(claims: Claims): AuthUser {
            val userId = claims["userId"]?.toString()?.toLong()
            val username = claims["username"] as? String
            val authString = claims["auth"] as? String

            requireNotNull(userId) { "userId is missing in claims" }
            requireNotNull(username) { "username is missing in claims" }
            requireNotNull(authString) { "authorities are missing in claims" }

            val authorities =
                authString
                    .split(",")
                    .filter { it.isNotBlank() }
                    .map { UserRole.from(it.trim()) }

            return AuthUser(userId, username, authorities)
        }
    }
}
