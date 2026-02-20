package com.jukebox.api.config

import com.jukebox.api.auth.oauth2.RedisOAuth2AuthorizedClientService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager

@Configuration
class StreamingServiceConfig {
    @Bean
    fun streamingServiceManager(
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientService: RedisOAuth2AuthorizedClientService,
    ): OAuth2AuthorizedClientManager {
        val authorizedClientRepository =
            AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService)
        val provider =
            OAuth2AuthorizedClientProviderBuilder
                .builder()
                .authorizationCode()
                // Automatically reissue access token when it's expired.
                .refreshToken()
                .build()

        return DefaultOAuth2AuthorizedClientManager(
            clientRegistrationRepository,
            authorizedClientRepository,
        ).apply {
            setAuthorizedClientProvider(provider)
        }
    }
}
