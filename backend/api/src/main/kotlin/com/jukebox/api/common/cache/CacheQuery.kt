package com.jukebox.api.common.cache

import com.jukebox.api.auth.dto.AuthUser
import java.time.Duration

enum class Cache(
    val prefix: String,
    val ttl: Duration,
) {
    AUTH_USER_TO_ISSUE("auth_user_to_issue", Duration.ofMinutes(1)),
    SESSION("session", Duration.ofDays(28)),
}

sealed class CacheQuery<T : Any>(
    val cache: Cache,
    val isNumeric: Boolean = false,
) {
    abstract fun toKey(): String

    data class AuthUserToIssue(
        val code: String,
    ) : CacheQuery<AuthUser>(Cache.AUTH_USER_TO_ISSUE) {
        override fun toKey() = "code:$code"
    }

    data class SessionByToken(
        val token: String,
    ) : CacheQuery<Long>(Cache.SESSION, isNumeric = true) {
        override fun toKey() = "refresh_token:$token"
    }

    data class SessionByUser(
        val userId: Long,
    ) : CacheQuery<String>(Cache.SESSION) {
        override fun toKey() = "user:$userId"
    }
}
