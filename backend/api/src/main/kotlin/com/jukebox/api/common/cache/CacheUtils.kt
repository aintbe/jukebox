package com.jukebox.api.common.cache

import org.springframework.cache.CacheManager

inline fun <reified T : Any> CacheManager.get(query: CacheQuery<T>): T? {
    val cache = this.getCache(query.cache.prefix)
    val key = query.toKey()

    return if (query.isNumeric) {
        val n = cache?.get(key, Number::class.java)
        when (T::class) {
            Double::class -> n?.toDouble()
            Float::class -> n?.toFloat()
            Int::class -> n?.toInt()
            Long::class -> n?.toLong()
            Short::class -> n?.toShort()
            Byte::class -> n?.toByte()
            else -> null
        } as T?
    } else {
        cache?.get(key, T::class.java)
    }
}

fun <T : Any> CacheManager.put(
    query: CacheQuery<T>,
    value: T,
) = this.getCache(query.cache.prefix)?.put(query.toKey(), value)

fun <T : Any> CacheManager.evict(query: CacheQuery<T>) = this.getCache(query.cache.prefix)?.evict(query.toKey())
