package com.jukebox.backend.config

import com.jukebox.backend.common.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.security.jackson.SecurityJacksonModules
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper

@Configuration
class RedisConfig(
    private val redisConnectionFactory: RedisConnectionFactory,
) {
    @Bean
    fun redisTemplate(): RedisTemplate<String?, Any?> =
        RedisTemplate<String?, Any?>().apply {
            connectionFactory = redisConnectionFactory
            defaultSerializer = StringRedisSerializer()
            afterPropertiesSet()
        }

    /**
     * A template dedicated to OAuth2 client management.
     * This will help us restore an [OAuth2AuthorizedClient] instance
     * easily from the cached json object.
     */
    @Bean
    fun oAuth2RedisTemplate(): RedisTemplate<String, OAuth2AuthorizedClient> {
        // Register class definitions so that Jackson understand how to unserialize stored data.
        val classMapper =
            JsonMapper
                .builder()
                .addModules(
                    SecurityJacksonModules.getModules(javaClass.classLoader),
                ).build()
        val classSerializer = JacksonJsonRedisSerializer(classMapper, OAuth2AuthorizedClient::class.java)

        return RedisTemplate<String, OAuth2AuthorizedClient>().apply {
            connectionFactory = redisConnectionFactory
            keySerializer = StringRedisSerializer()
            valueSerializer = classSerializer
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = classSerializer
        }
    }

    @Bean
    fun cacheManager(objectMapper: ObjectMapper): CacheManager {
        // Serialize cache data in `String:json` format.
        val defaultConfig =
            RedisCacheConfiguration
                .defaultCacheConfig()
                .serializeKeysWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()),
                ).serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(
                        GenericJacksonJsonRedisSerializer(objectMapper),
                    ),
                )

        // Create different caches for each cache key prefix to apply different ttl.
        val configs =
            Cache.entries.associate { it.prefix to defaultConfig.entryTtl(it.ttl) }

        return RedisCacheManager
            .builder(redisConnectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(configs)
            .build()
    }
}
