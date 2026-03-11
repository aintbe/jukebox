package com.jukebox.api.common.cache

import com.jukebox.api.auth.dto.AuthUser
import com.jukebox.core.cache.CacheConfig
import com.jukebox.core.cache.CacheQuery
import java.time.Duration

object Cache {
    data object AuthUserToIssue : CacheConfig<AuthUser>("auth_user_to_issue", Duration.ofMinutes(100)) {
        operator fun invoke(ticket: String) = CacheQuery(this, "ticket:$ticket")
    }

    data object SessionByToken : CacheConfig<Long>("session_refresh_token", Duration.ofDays(28)) {
        operator fun invoke(token: String) = CacheQuery(this, token)
    }

    data object SessionByUser : CacheConfig<String>("session_user_id", Duration.ofDays(28)) {
        operator fun invoke(userId: Long) = CacheQuery(this, userId.toString())
    }
}
