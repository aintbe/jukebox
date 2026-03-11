package com.jukebox.api.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
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
import tools.jackson.databind.DefaultTyping
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator

@Configuration
@EnableCaching
class RedisConfig(
    private val redisConnectionFactory: RedisConnectionFactory,
) {
    /**
     * Provides a RedisTemplate bean for manual, programmatic cache operations.
     * Use this with [com.jukebox.api.common.cache.CacheService] to systemically
     * get/set data without worrying about type conversions.
     */
    @Bean
    fun redisTemplate(): RedisTemplate<String, Any> {
        val jsonSerializer = JacksonJsonRedisSerializer(Any::class.java)

        return RedisTemplate<String, Any>().apply {
            connectionFactory = redisConnectionFactory
            keySerializer = StringRedisSerializer()
            valueSerializer = jsonSerializer
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = jsonSerializer
        }
    }

    /**
     * A template dedicated to OAuth2 client management. Do not merge this
     * with [redisTemplate], since [redisTemplate] does not understand [OAuth2AuthorizedClient]
     * or any subtypes and thus cannot deserialize it.
     */
    @Bean
    fun oAuth2RedisTemplate(): RedisTemplate<String, OAuth2AuthorizedClient> {
        // Register class definitions so that Jackson understand how to deserialize stored data.
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

    /**
     * Provides a CacheManager bean that powers Spring's annotation-driven caching
     * abstraction (@Cacheable, @CacheEvict, @CachePut, etc.). All cache operations
     * declared via annotations will be routed through this manager.
     */
    @Bean
    fun cacheManager(): CacheManager {
        // Store class information only if it matches following criteria.
        val validator =
            BasicPolymorphicTypeValidator
                .builder()
                .allowIfBaseType("com.jukebox.")
                .allowIfBaseType(java.util.Collection::class.java)
                .build()

        val classMapper =
            JsonMapper
                .builder()
                .polymorphicTypeValidator(validator)
                .activateDefaultTyping(
                    validator,
                    DefaultTyping.NON_FINAL,
                    JsonTypeInfo.As.PROPERTY,
                ).build()

        // Serialize cache data in `String:Json` format.
        val defaultConfig =
            RedisCacheConfiguration
                .defaultCacheConfig()
                .serializeKeysWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()),
                ).serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(
                        GenericJacksonJsonRedisSerializer(classMapper),
                    ),
                )

        return RedisCacheManager
            .builder(redisConnectionFactory)
            .cacheDefaults(defaultConfig)
            .build()
    }
}
