package com.jukebox.backend.auth.oauth2

import com.jukebox.backend.config.properties.EndpointProperties
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2FailureHandler(
    private val endpointProperties: EndpointProperties,
) : SimpleUrlAuthenticationFailureHandler() {
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException,
    ) {
        val url =
            UriComponentsBuilder
                .fromUriString(endpointProperties.oAuth2CallbackUrl)
                .build()
                .toUriString()
        redirectStrategy.sendRedirect(request, response, url)
    }
}
