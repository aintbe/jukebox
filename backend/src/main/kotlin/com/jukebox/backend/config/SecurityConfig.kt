package com.jukebox.backend.config

import com.jukebox.backend.auth.jwt.JwtAuthenticationFilter
import com.jukebox.backend.auth.jwt.JwtProvider
import com.jukebox.backend.auth.oauth2.OAuth2FailureHandler
import com.jukebox.backend.auth.oauth2.OAuth2SuccessHandler
import com.jukebox.backend.auth.oauth2.OAuth2UserService
import com.jukebox.backend.auth.oauth2.RedisOAuth2AuthorizedClientService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val oAuth2UserService: OAuth2UserService,
    private val oAuth2AuthorizedClientService: RedisOAuth2AuthorizedClientService,
    private val oAuth2SuccessHandler: OAuth2SuccessHandler,
    private val oAuth2FailureHandler: OAuth2FailureHandler,
    private val jwtProvider: JwtProvider,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
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
                JwtAuthenticationFilter(jwtProvider),
                UsernamePasswordAuthenticationFilter::class.java,
            )

        return http.build()
    }
}
