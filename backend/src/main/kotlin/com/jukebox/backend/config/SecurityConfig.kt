package com.jukebox.backend.config

import com.jukebox.backend.auth.jwt.JwtAuthenticationFilter
import com.jukebox.backend.auth.jwt.JwtHttpHandler
import com.jukebox.backend.auth.jwt.JwtProvider
import com.jukebox.backend.auth.oauth2.OAuth2FailureHandler
import com.jukebox.backend.auth.oauth2.OAuth2SuccessHandler
import com.jukebox.backend.auth.oauth2.OAuth2UserService
import com.jukebox.backend.auth.oauth2.RedisOAuth2AuthorizedClientService
import com.jukebox.backend.config.properties.EndpointProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig {
    @Bean
    fun filterChain(
        http: HttpSecurity,
        oAuth2UserService: OAuth2UserService,
        oAuth2AuthorizedClientService: RedisOAuth2AuthorizedClientService,
        oAuth2SuccessHandler: OAuth2SuccessHandler,
        oAuth2FailureHandler: OAuth2FailureHandler,
        jwtProvider: JwtProvider,
        jwtHttpHandler: JwtHttpHandler,
    ): SecurityFilterChain {
        http
            // Add global CORS setting. If source is not explicitly provided,
            // Spring Security will look for [CorsConfigurationSource] bean by default.
            // cf. https://docs.spring.io/spring-security/reference/servlet/integrations/cors.html
            .cors { }
            // Disable default login functionality that are not used in backend.
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .logout { it.disable() }
            // Disable csrf and session as we're using JWT-based authentications.
            .csrf { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            // Customize OAuth2 security settings.
            .oauth2Login {
                it
                    // Spring Security automatically retrieves authorization code, access token
                    // and user profile using specified uri in `application.yml`. And then it calls
                    // [OAuthUserService.loadUser] to generate [Principal].
                    .userInfoEndpoint { c -> c.userService(oAuth2UserService) }
                    // This registers relayed principal in Redis to use it later.
                    .authorizedClientService(oAuth2AuthorizedClientService)
                    // Generate JWT using the relayed principal for authentication in app.
                    .successHandler(oAuth2SuccessHandler)
                    .failureHandler(oAuth2FailureHandler)
            }
            // Add JWT authentication filter before default authentication filter
            // so that the next filters & interceptors could work with context as expected.
            .addFilterBefore(
                JwtAuthenticationFilter(jwtProvider, jwtHttpHandler),
                UsernamePasswordAuthenticationFilter::class.java,
            )
            // Spring security redirects user to login page when authentication fails.
            // Intercept the error and return error response right away.
            .exceptionHandling { handling ->
                handling.authenticationEntryPoint { request, response, authException ->
                    response.apply {
                        status = HttpStatus.UNAUTHORIZED.value()
                        writer.write(authException.message ?: "")
                    }
                }
            }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(endpointProperties: EndpointProperties): CorsConfigurationSource {
        val configuration =
            CorsConfiguration().apply {
                allowedOrigins = listOf(endpointProperties.frontend.domain)
                allowedMethods = listOf("POST", "GET", "DELETE", "PUT")
                allowedHeaders = listOf("*")
                exposedHeaders = listOf("Content-Type", "Authorization")
                allowCredentials = true
            }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
