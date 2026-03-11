package com.jukebox.api.jukebox

import com.jukebox.api.auth.dto.AuthUser
import com.jukebox.api.auth.jwt.TokenProvider
import com.jukebox.api.common.cache.CacheService
import com.jukebox.api.jukebox.dto.toCached
import com.jukebox.core.cache.SharedCache
import com.jukebox.core.exception.EntityNotFoundException
import com.jukebox.core.exception.JukeboxUnavailableException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class JukeboxService(
    private val jukeboxRepository: JukeboxRepository,
    private val tokenProvider: TokenProvider,
    private val cacheService: CacheService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun getJukebox(handle: String) =
        jukeboxRepository.findByHandle(handle)
            ?: throw EntityNotFoundException("jukebox", handle)

    fun joinJukebox(
        handle: String,
        user: AuthUser?,
    ): String {
        val jukeboxSession =
            getJukebox(handle).toCached(user?.userId)
                ?: throw JukeboxUnavailableException()

        val ticket = tokenProvider.generateRandomToken()
        cacheService.put(
            SharedCache.JukeboxWebsocketSession(ticket),
            jukeboxSession,
        )
        return ticket
    }
}
