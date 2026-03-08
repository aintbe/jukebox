package com.jukebox.api.auth.jwt

import com.jukebox.api.config.properties.JwtProperties
import com.jukebox.core.constants.HttpConstants
import com.jukebox.core.exception.UnauthenticatedException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component

@Component
class TokenHttpHandler(
    private val jwtProperties: JwtProperties,
) {
    private enum class CookieAction { CREATE, DELETE }

    private fun setRefreshCookie(
        token: String,
        action: CookieAction,
    ): ResponseCookie =
        ResponseCookie
            .from("refreshToken", token)
            .path("/auth/reissue")
            .maxAge(
                when (action) {
                    CookieAction.CREATE -> jwtProperties.refreshExpiresIn.toLong()
                    CookieAction.DELETE -> 0
                },
            ).httpOnly(true)
            .secure(false) // TODO: change to use .env
            .sameSite("Lax")
            .build()

    fun createRefreshCookie(token: String) = setRefreshCookie(token, CookieAction.CREATE)

    fun deleteRefreshCookie() = setRefreshCookie("", CookieAction.DELETE)

    fun extractAccessToken(request: HttpServletRequest): String {
        val bearer = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (bearer != null && bearer.startsWith("${HttpConstants.AUTHORIZATION_TYPE} ")) {
            return bearer.substring("${HttpConstants.AUTHORIZATION_TYPE} ".length)
        }
        throw UnauthenticatedException("MISSING_JWT", "Authorization header is missing.")
    }
}
