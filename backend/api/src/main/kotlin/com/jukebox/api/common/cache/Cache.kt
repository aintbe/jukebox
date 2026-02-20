package com.jukebox.api.common.cache

import org.springframework.cache.CacheManager
import java.time.Duration

enum class Cache(
    val prefix: String,
    val ttl: Duration,
) {
    REFRESH_TOKEN("refresh_token", Duration.ofDays(28)),
}

inline fun <reified T : Any> CacheManager.get(
    cache: Cache,
    key: String,
): T? = this.getCache(cache.prefix)?.get(key, T::class.java)

inline fun <reified T : Any> CacheManager.put(
    cache: Cache,
    key: String,
    value: T,
) = this.getCache(cache.prefix)?.put(key, value)
