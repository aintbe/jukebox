package com.jukebox.relay.common.cache

import com.jukebox.core.cache.CacheQuery
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.time.Duration

@Component
class CacheService(
    val reactiveRedisTemplate: ReactiveRedisTemplate<String, Any>,
    val objectMapper: ObjectMapper,
) {
    final suspend inline fun <reified T : Any> get(query: CacheQuery<T>): T? {
        val rawValue =
            reactiveRedisTemplate
                .opsForValue()
                .get(query.fullKey)
                .awaitSingleOrNull() ?: return null
        return objectMapper.convertValue(rawValue, T::class.java)
    }

    suspend fun <T : Any> put(
        query: CacheQuery<T>,
        value: T,
        ttl: Duration? = null,
    ) {
        val timeout = ttl ?: query.cache.ttl
        if (timeout != null) {
            reactiveRedisTemplate.opsForValue().set(query.fullKey, value, timeout)
        } else {
            reactiveRedisTemplate.opsForValue().set(query.fullKey, value)
        }.awaitSingleOrNull()
    }

    suspend fun <T : Any> delete(query: CacheQuery<T>) =
        reactiveRedisTemplate
            .delete(query.fullKey)
            .awaitSingleOrNull()
}
