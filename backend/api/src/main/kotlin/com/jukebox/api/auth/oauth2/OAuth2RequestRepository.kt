package com.jukebox.api.auth.oauth2

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component
import java.net.URLEncoder

@Component
class OAuth2RequestRepository : AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    private val delegate = HttpSessionOAuth2AuthorizationRequestRepository()

    override fun loadAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? =
        delegate.loadAuthorizationRequest(request)

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val newRequest =
            OAuth2AuthorizationRequest
                .from(authorizationRequest)
                // After the authorization server redirects the request to Spring,
                // Spring Security internally compares the stored state and the state information
                // inside redirected request/response to check if the request/response is valid.
                // The state information returned in request/response is encoded in Base64URL,
                // but the stored state is encoded in Base64. However, the default repository
                // does not properly handle these differences, assuming the request is "unrecognizable"
                // when it has an escaped character.
                .state(URLEncoder.encode(authorizationRequest?.state, "UTF-8"))
                .build()
        delegate.saveAuthorizationRequest(newRequest, request, response)
    }

    override fun removeAuthorizationRequest(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): OAuth2AuthorizationRequest? = delegate.removeAuthorizationRequest(request, response)
}
