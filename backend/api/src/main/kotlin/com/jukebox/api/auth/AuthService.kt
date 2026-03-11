package com.jukebox.api.auth

import com.jukebox.api.auth.dto.AuthInfo
import com.jukebox.api.auth.dto.AuthUser
import com.jukebox.api.auth.jwt.TokenProvider
import com.jukebox.api.common.cache.Cache
import com.jukebox.api.common.cache.CacheService
import com.jukebox.api.user.UserRepository
import com.jukebox.core.exception.UnauthenticatedException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val cacheService: CacheService,
    private val tokenProvider: TokenProvider,
    private val userRepository: UserRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun signOut(user: AuthUser) {
        val cacheQuery = Cache.SessionByUser(user.userId)
        cacheService.get(cacheQuery)?.let { token ->
            cacheService.delete(cacheQuery)
            cacheService.delete(Cache.SessionByToken(token))
        }
    }

    fun issueTokens(ticket: String): AuthInfo.Tokens {
        val userQuery = Cache.AuthUserToIssue(ticket)
        val user =
            cacheService.get(userQuery)?.also {
                cacheService.delete(userQuery)
            } ?: throw UnauthenticatedException(
                "INVALID_AUTH_TICKET",
                "Cannot identify user from auth ticket: $ticket",
            )

        val accessToken = tokenProvider.generateAccessToken(user)

        // We don't have to create JWT for refresh tokens as the server keeps their states
        val refreshToken = tokenProvider.generateRandomToken()
        cacheService.get(Cache.SessionByUser(user.userId))?.also { existingToken ->
            cacheService.delete(Cache.SessionByToken(existingToken))
        }
        cacheService.put(Cache.SessionByToken(refreshToken), user.userId)
        cacheService.put(Cache.SessionByUser(user.userId), refreshToken)

        return AuthInfo.Tokens(accessToken, refreshToken)
    }

    fun reissueAccessToken(refreshToken: String): String {
        val userId = cacheService.get(Cache.SessionByToken(refreshToken))
        val user = userId?.let { userRepository.findUserById(userId) }
        if (user == null) {
            throw UnauthenticatedException(
                "INVALID_REFRESH_TOKEN",
                "Cannot identify user from refresh token.",
            )
        }
        return tokenProvider.generateAccessToken(AuthUser.from(user))
    }
}
