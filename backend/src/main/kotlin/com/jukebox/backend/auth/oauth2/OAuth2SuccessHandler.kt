package com.jukebox.backend.auth.oauth2

import com.jukebox.backend.auth.dto.OAuth2Principal
import com.jukebox.backend.auth.jwt.JwtHttpHandler
import com.jukebox.backend.auth.jwt.JwtProvider
import com.jukebox.backend.config.properties.EndpointProperties
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2SuccessHandler(
    private val failureHandler: OAuth2FailureHandler,
    private val jwtProvider: JwtProvider,
    private val jwtHttpHandler: JwtHttpHandler,
    private val endpointProperties: EndpointProperties,
) : SimpleUrlAuthenticationSuccessHandler() {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val user =
            (authentication.principal as? OAuth2Principal)?.requestUser
        if (user == null) {
            return failureHandler.onAuthenticationFailure(
                request,
                response,
                OAuth2AuthenticationException("PARSE_ERROR"),
            )
        }

        val accessToken = jwtProvider.generateAccessToken(user)
        val url =
            UriComponentsBuilder
                .fromUriString(endpointProperties.oAuth2CallbackUrl)
                // Response header is not relayed after redirection.
                .queryParam("access_token", accessToken)
                .build()
                .toUriString()

        jwtProvider.generateRefreshToken(user).let {
            jwtHttpHandler.setRefreshToken(response, it)
        }
        redirectStrategy.sendRedirect(request, response, url)
    }
}
