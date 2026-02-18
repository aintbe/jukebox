package com.jukebox.backend.auth

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val redisTemplate: RedisTemplate<String, Any?>,
) {
    private val log = LoggerFactory.getLogger(javaClass)
}
