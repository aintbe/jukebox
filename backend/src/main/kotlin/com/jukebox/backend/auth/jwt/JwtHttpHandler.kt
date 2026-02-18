package com.jukebox.backend.auth.jwt

import com.jukebox.backend.config.properties.JwtProperties
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component

@Component
class JwtHttpHandler(
    private val jwtProperties: JwtProperties,
) {
    fun setAccessToken(
        response: HttpServletResponse,
        token: String,
    ) {
        response.setHeader("Authorization", "Bearer $token")
    }

    fun setRefreshToken(
        response: HttpServletResponse,
        token: String,
    ) {
        val refreshCookie =
            Cookie("refreshToken", token).apply {
                isHttpOnly = true
                secure = false // TODO: change to use .env
                path = "/auth/reissue"
                maxAge = jwtProperties.refreshExpiresIn
            }
        response.addCookie(refreshCookie)
    }

    fun extractAccessToken(request: HttpServletRequest): String? {
        val bearer = request.getHeader("Authorization")
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring("Bearer ".length)
        }
        return null
    }
}
