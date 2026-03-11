package com.jukebox.api.auth

import com.jukebox.api.auth.dto.AuthDto
import com.jukebox.api.auth.dto.AuthUser
import com.jukebox.api.auth.jwt.TokenHttpHandler
import com.jukebox.core.constants.HttpConstants
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
@PreAuthorize("isAnonymous()")
class AuthController(
    private val authService: AuthService,
    private val tokenHttpHandler: TokenHttpHandler,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/sign-out")
    @PreAuthorize("isAuthenticated()")
    fun signOut(
        @AuthenticationPrincipal user: AuthUser,
    ): ResponseEntity<Unit> {
        authService.signOut(user)
        val cookie = tokenHttpHandler.deleteRefreshCookie()
        return ResponseEntity
            .ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .build()
    }

    @PostMapping("/issue")
    fun issue(
        @RequestBody request: AuthDto.IssueRequest,
    ): ResponseEntity<Unit> {
        val tokens = authService.issueTokens(request.ticket)
        val cookie = tokenHttpHandler.createRefreshCookie(tokens.refreshToken)
        return ResponseEntity
            .ok()
            .header(HttpHeaders.AUTHORIZATION, "${HttpConstants.AUTHORIZATION_TYPE} ${tokens.accessToken}")
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .build()
    }

    @PostMapping("/reissue")
    fun reissue(
        @CookieValue refreshToken: String,
    ): ResponseEntity<Unit> {
        val token = authService.reissueAccessToken(refreshToken)
        return ResponseEntity
            .ok()
            .header(HttpHeaders.AUTHORIZATION, "${HttpConstants.AUTHORIZATION_TYPE} $token")
            .build()
    }
}
