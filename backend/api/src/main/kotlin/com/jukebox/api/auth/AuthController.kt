package com.jukebox.api.auth

import com.jukebox.api.auth.oauth2.RedisOAuth2AuthorizedClientService
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
@PreAuthorize("isAnonymous()")
class AuthController(
    private val authService: AuthService,
    private val redis: RedisOAuth2AuthorizedClientService,
) {
    val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/reissue")
    fun reissue(
        @CookieValue refreshToken: String,
        // TODO: add reissue logic
    ) = null
}
