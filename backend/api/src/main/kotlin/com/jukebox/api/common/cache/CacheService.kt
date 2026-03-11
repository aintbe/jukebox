package com.jukebox.api.common.cache

import com.jukebox.core.cache.CacheQuery
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.time.Duration

@Component
class CacheService(
    val redisTemplate: RedisTemplate<String, Any>,
    val objectMapper: ObjectMapper,
) {
    final inline fun <reified T : Any> get(query: CacheQuery<T>): T? =
        redisTemplate
            .opsForValue()
            .get(query.fullKey)
            ?.let { rawValue ->
                objectMapper.convertValue(rawValue, T::class.java)
            }

    fun <T : Any> put(
        query: CacheQuery<T>,
        value: T,
        ttl: Duration? = null,
    ) {
        val timeout = ttl ?: query.cache.ttl
        if (timeout != null) {
            redisTemplate.opsForValue().set(query.fullKey, value, timeout)
        } else {
            redisTemplate.opsForValue().set(query.fullKey, value)
        }
    }

    fun <T : Any> delete(query: CacheQuery<T>) = redisTemplate.delete(query.fullKey)
}
