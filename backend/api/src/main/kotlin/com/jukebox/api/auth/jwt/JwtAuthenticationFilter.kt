package com.jukebox.api.auth.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val jwtHttpHandler: JwtHttpHandler,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        val token = jwtHttpHandler.extractAccessToken(request)
        val principal = token?.let { jwtProvider.extractPrincipal(it) }

        if (principal != null) {
            val authentication: Authentication =
                UsernamePasswordAuthenticationToken(principal, null, principal.authorities)
            SecurityContextHolder.getContext().authentication = authentication
        }
        chain.doFilter(request, response)
    }
}
