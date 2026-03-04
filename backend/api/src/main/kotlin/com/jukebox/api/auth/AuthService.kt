package com.jukebox.api.auth

import com.jukebox.api.auth.dto.AuthDto
import com.jukebox.api.auth.dto.AuthUser
import com.jukebox.api.auth.jwt.TokenProvider
import com.jukebox.api.common.cache.CacheQuery
import com.jukebox.api.common.cache.evict
import com.jukebox.api.common.cache.get
import com.jukebox.api.user.UserRepository
import com.jukebox.core.exception.UnauthenticatedException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val cacheManager: CacheManager,
    private val tokenProvider: TokenProvider,
    private val userRepository: UserRepository,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    fun signOut(user: AuthUser) {
        val cacheQuery = CacheQuery.SessionByUser(user.userId)
        cacheManager.get(cacheQuery)?.let { token ->
            cacheManager.evict(cacheQuery)
            cacheManager.evict(CacheQuery.SessionByToken(token))
        }
    }

    fun issueTokens(code: String): AuthDto.Tokens {
        val cacheQuery = CacheQuery.AuthUserToIssue(code)
        val user =
            cacheManager.get(cacheQuery)?.also {
                cacheManager.evict(cacheQuery)
            }
                ?: throw UnauthenticatedException(
                    "INVALID_AUTH_CODE",
                    "Cannot identify user from auth code: $code",
                )

        val accessToken = tokenProvider.generateAccessToken(user)
        val refreshToken = tokenProvider.generateRefreshToken(user)
        return AuthDto.Tokens(accessToken, refreshToken)
    }

    fun reissueAccessToken(refreshToken: String): String {
        val userId = cacheManager.get(CacheQuery.SessionByToken(refreshToken))
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
