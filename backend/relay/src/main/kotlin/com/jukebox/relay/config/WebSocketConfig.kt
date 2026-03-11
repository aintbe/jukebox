package com.jukebox.relay.config

import com.jukebox.core.properties.EndpointProperties
import com.jukebox.relay.jukebox.JukeboxWebsocketController
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter

@Configuration
class WebSocketConfig {
    @Bean
    fun handlerMapping(
        jukeboxWebsocketController: JukeboxWebsocketController,
        endpointProperties: EndpointProperties,
    ): HandlerMapping {
        val corsConfig =
            CorsConfiguration().apply {
                allowedOrigins = listOf(endpointProperties.frontend.domain)
                allowedMethods = listOf("*")
                allowedHeaders = listOf("*")
            }

        return SimpleUrlHandlerMapping().apply {
            urlMap = mapOf("/ws/jukebox" to jukeboxWebsocketController)
            order = Ordered.HIGHEST_PRECEDENCE
            setCorsConfigurations(mapOf("/ws/jukebox" to corsConfig))
        }
    }

    @Bean
    fun handlerAdapter() = WebSocketHandlerAdapter()
}
