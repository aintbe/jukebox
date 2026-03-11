package com.jukebox.core.cache

import org.springframework.core.ResolvableType
import java.time.Duration

/**
 * NOTE: To register cache names and ttl in [CacheManager], you need to create a
 *      "data object" inheriting this class and register them with [RedisConfig].
 *      You don't need to define `invoke` if [Cacheable] is the only
 *      accessor of that cache name.
 */
abstract class CacheConfig<R : Any>(
    val prefix: String,
    val ttl: Duration? = null,
) {
    val responseClass: Class<*> =
        ResolvableType
            .forClass(javaClass)
            .`as`(this::class.java)
            .resolveGeneric(0) ?: Any::class.java
}

class CacheQuery<R : Any>(
    val cache: CacheConfig<R>,
    key: String,
) {
    val fullKey = "${cache.prefix}::$key"
}
