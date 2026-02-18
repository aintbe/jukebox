package com.jukebox.backend.auth.jwt

import com.jukebox.backend.auth.dto.AuthUser
import com.jukebox.backend.common.cache.Cache
import com.jukebox.backend.common.cache.put
import com.jukebox.backend.config.properties.JwtProperties
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.IncorrectClaimException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Date
import java.util.UUID

@Component
class JwtProvider(
    private val jwtProperties: JwtProperties,
    private val cacheManager: CacheManager,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun generateAccessToken(user: AuthUser): String {
        val now = Instant.now()
        return Jwts
            .builder()
            .setIssuedAt(Date.from(now))
            .setExpiration(
                Date.from(
                    now.plusSeconds(
                        jwtProperties.accessExpiresIn.toLong(),
                    ),
                ),
            ).setIssuer(jwtProperties.issuer)
            .claim("userId", user.userId)
            .claim("username", user.username)
            // TODO: find actual roles
            .claim("auth", "USER_ROLE")
            .signWith(jwtProperties.key)
            .compact()
    }

    fun generateRefreshToken(user: AuthUser): String {
        // We don't have to create JWT for refresh tokens as the server keeps their states
        val token = UUID.randomUUID().toString()
        cacheManager.put(Cache.REFRESH_TOKEN, "user:${user.userId}", token)

        return token
    }

    fun extractPrincipal(token: String): AuthUser? =
        runCatching {
            val claims =
                Jwts
                    .parserBuilder()
                    .setSigningKey(jwtProperties.key)
                    .requireIssuer(jwtProperties.issuer)
                    .build()
                    .parseClaimsJws(token)
                    .body

            return AuthUser.from(claims)
        }.onFailure { e ->
            val message =
                when (e) {
                    is SecurityException, is MalformedJwtException -> "Invalid JWT"
                    is ExpiredJwtException -> "Expired JWT"
                    is IncorrectClaimException -> "JWT claim does not meet requirements"
                    is IllegalArgumentException -> "JWT claims string is empty or invalid"
                    is UnsupportedJwtException -> "Unsupported JWT"
                    else -> "Error occurred while parsing JWT"
                }
            log.debug(message, e)
        }.getOrNull()
}
