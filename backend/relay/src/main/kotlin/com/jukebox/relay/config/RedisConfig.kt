package com.jukebox.relay.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {
    @Bean
    fun reactiveRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, Any> {
        val context =
            RedisSerializationContext
                .newSerializationContext<String, Any>(StringRedisSerializer())
                .value(JacksonJsonRedisSerializer(Any::class.java))
                .build()

        return ReactiveRedisTemplate(factory, context)
    }
}
