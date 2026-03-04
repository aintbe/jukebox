package com.jukebox.api.jukebox.web

import com.jukebox.api.streamingservice.StreamingServiceRepository
import com.jukebox.core.dto.RequestContext
import com.jukebox.core.dto.StreamingAccess
import com.jukebox.core.exception.BindingException
import com.jukebox.core.exception.StreamingServiceAuthRequiredException
import com.jukebox.core.exception.StreamingServiceNotFoundException
import com.jukebox.core.exception.UnauthenticatedException
import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.HandlerMapping

@Component
class RequestContextResolver(
    private val streamingServiceRepository: StreamingServiceRepository,
    private val streamingManager: OAuth2AuthorizedClientManager,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(CurrentRequestContext::class.java)

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any? {
        val pathVariables =
            webRequest.getAttribute(
                HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
                RequestAttributes.SCOPE_REQUEST,
            ) as? Map<*, *>

        val jukeboxId =
            pathVariables?.get("jukeboxId")?.toString()?.toLongOrNull()
                ?: throw BindingException("`jukeboxId` is missing or not a number.")

        val authentication = SecurityContextHolder.getContext().authentication
        val userId =
            authentication?.name?.toLongOrNull()
                ?: throw UnauthenticatedException(
                    "UNAUTHENTICATED",
                    "You must pass Authorization header to use this endpoint.",
                )

        val streamingService =
            streamingServiceRepository.findByUserId(userId)
                ?: throw StreamingServiceNotFoundException()

        // Below works as same as [RegisteredOAuth2AuthorizedClient] annotation,
        // which means Spring automatically reissues access token if it's expired.
        val authorizeRequest =
            OAuth2AuthorizeRequest
                .withClientRegistrationId(streamingService.name)
                .principal(authentication)
                .build()
        val authorizedClient =
            streamingManager.authorize(authorizeRequest)
                ?: throw StreamingServiceAuthRequiredException(streamingService.name)
        val accessToken = authorizedClient.accessToken

        return RequestContext(
            jukeboxId,
            userId,
            StreamingAccess(
                serviceName = streamingService.name,
                token = accessToken.tokenValue,
                expiresAt = accessToken.expiresAt,
            ),
        )
    }
}
