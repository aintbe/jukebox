package com.jukebox.backend.config.properties

import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.boot.context.properties.ConfigurationProperties
import java.security.Key

// Spring injects a property named `jwt` from `application.yml`
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    private val secret: String,
    val accessExpiresIn: Int,
    val refreshExpiresIn: Int,
    val issuer: String,
) {
    val key: Key =
        Decoders.BASE64.decode(secret).let { secretBytes ->
            Keys.hmacShaKeyFor(secretBytes)
        }
}
