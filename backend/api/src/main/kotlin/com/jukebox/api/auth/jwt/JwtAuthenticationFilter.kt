package com.jukebox.api.auth.jwt

import com.jukebox.core.dto.BusinessExceptionDto
import com.jukebox.core.exception.UnauthenticatedException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val tokenProvider: TokenProvider,
    private val tokenHttpHandler: TokenHttpHandler,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        try {
            val token = tokenHttpHandler.extractAccessToken(request)
            val principal = tokenProvider.extractPrincipal(token)

            val authentication: Authentication =
                UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    principal.authorities.map {
                        SimpleGrantedAuthority(it.name)
                    },
                )
            SecurityContextHolder.getContext().authentication = authentication
            MDC.put("userId", principal.name) // Display user ID in logs.
        } catch (e: Exception) {
            // Store occurred exception to add it into the response later.
            // Check [SecurityConfig.filterChain > authenticationEntryPoint] for usage.
            if (e is UnauthenticatedException) {
                request.setAttribute("AUTHENTICATION_FILTER_ERROR", BusinessExceptionDto.from(e))
            }
        }
        chain.doFilter(request, response)
        MDC.clear()
    }
}
