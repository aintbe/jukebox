package com.jukebox.backend.auth.dto

import com.jukebox.backend.user.entity.User
import io.jsonwebtoken.Claims
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.security.Principal

class AuthUser private constructor(
    val userId: Long,
    val username: String,
    val authorities: List<GrantedAuthority>,
) : Principal {
    override fun getName(): String? = userId.toString()

    companion object {
        fun from(user: User): AuthUser =
            AuthUser(
                userId = user.savedId,
                username = user.username,
                // TODO: add actual roles
                authorities = listOf(SimpleGrantedAuthority("USER_ROLE")),
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
                    .map { SimpleGrantedAuthority(it.trim()) }

            return AuthUser(userId, username, authorities)
        }
    }
}
