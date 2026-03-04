package com.jukebox.api.auth.jwt

import com.jukebox.api.auth.dto.AuthUser
import com.jukebox.api.common.cache.CacheQuery
import com.jukebox.api.common.cache.put
import com.jukebox.api.config.properties.JwtProperties
import com.jukebox.core.exception.UnauthenticatedException
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
class TokenProvider(
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
        cacheManager.put(CacheQuery.SessionByToken(token), user.userId)
        cacheManager.put(CacheQuery.SessionByUser(user.userId), token)
        return token
    }

    fun generateAuthCode(user: AuthUser): String {
        val code = UUID.randomUUID().toString()
        cacheManager.put(CacheQuery.AuthUserToIssue(code), user)
        return code
    }

    fun extractPrincipal(token: String): AuthUser {
        try {
            val claims =
                Jwts
                    .parserBuilder()
                    .setSigningKey(jwtProperties.key)
                    .requireIssuer(jwtProperties.issuer)
                    .build()
                    .parseClaimsJws(token)
                    .body

            return AuthUser.from(claims)
        } catch (e: Exception) {
            val (errorCode, message) =
                when (e) {
                    is SecurityException, is MalformedJwtException, is IncorrectClaimException,
                    is IllegalArgumentException, is UnsupportedJwtException,
                    -> "INVALID_JWT" to "Requested token has invalid format."

                    is ExpiredJwtException -> "EXPIRED_JWT" to "Please reissue access token."

                    else -> "INVALID_JWT" to "Error occurred while parsing JWT."
                }
            throw UnauthenticatedException(errorCode, message)
        }
    }
}
