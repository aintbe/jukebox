package com.jukebox.backend.auth

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
@PreAuthorize("isAnonymous()")
class AuthController {
    @GetMapping("/reissue")
    fun reissue(
        @CookieValue refreshToken: String,
        // TODO: add reissue logic
    ) = null
}
