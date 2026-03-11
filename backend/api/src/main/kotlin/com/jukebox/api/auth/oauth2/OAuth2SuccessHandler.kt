package com.jukebox.api.auth.oauth2

import com.jukebox.api.auth.dto.OAuth2Principal
import com.jukebox.api.auth.jwt.TokenProvider
import com.jukebox.api.common.cache.Cache
import com.jukebox.api.common.cache.CacheService
import com.jukebox.core.properties.EndpointProperties
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
    private val tokenProvider: TokenProvider,
    private val endpointProperties: EndpointProperties,
    private val cacheService: CacheService,
) : SimpleUrlAuthenticationSuccessHandler() {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val user =
            (authentication.principal as? OAuth2Principal)?.authUser
        if (user == null) {
            return failureHandler.onAuthenticationFailure(
                request,
                response,
                OAuth2AuthenticationException("PARSE_ERROR"),
            )
        }

        val ticket = tokenProvider.generateRandomToken()
        cacheService.put(Cache.AuthUserToIssue(ticket), user)

        val url =
            UriComponentsBuilder
                .fromUriString(endpointProperties.oAuth2CallbackUrl)
                // Response header is not relayed after redirection;
                // we need to use query params to maintain it.
                .queryParam("ticket", ticket)
                .build()
                .toUriString()
        redirectStrategy.sendRedirect(request, response, url)
    }
}
